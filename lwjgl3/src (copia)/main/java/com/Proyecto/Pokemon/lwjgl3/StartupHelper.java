/*
 * Copyright 2020 damios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Nota: la licencia y el copyright anteriores se aplican solo a este archivo.

package com.Proyecto.Pokemon.lwjgl3;

import com.badlogic.gdx.Version;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;
import org.lwjgl.system.macosx.LibC;
import org.lwjgl.system.macosx.ObjCRuntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import static org.lwjgl.system.JNI.invokePPP;
import static org.lwjgl.system.JNI.invokePPZ;
import static org.lwjgl.system.macosx.ObjCRuntime.objc_getClass;
import static org.lwjgl.system.macosx.ObjCRuntime.sel_getUid;

/**
 * Añade utilidades para asegurar que la JVM se inicie con el argumento
 * {@code -XstartOnFirstThread}, necesario en macOS para que LWJGL 3 funcione.
 * Tambien ayuda en Windows cuando los nombres de usuario contienen caracteres
 * no latinos.
 */
public class StartupHelper {

    private static final String JVM_RESTARTED_ARG = "jvmIsRestarted";

    private StartupHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Inicia una nueva JVM si la aplicacion se inicio en macOS sin el argumento
     * {@code -XstartOnFirstThread}. Tambien incluye codigo para Windows para evitar
     * fallos cuando la ruta de inicio del usuario contiene caracteres especiales.
     *
     * @param redirectOutput Define si la salida de la nueva JVM debe redirigirse a
     *                       la antigua.
     * @return true si se inicio una nueva JVM (y por tanto no debe ejecutarse mas
     *         codigo en la actual).
     */
    public static boolean startNewJvmIfRequired(boolean redirectOutput) {
        String osName = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT);
        if (!osName.contains("mac")) {
            if (osName.contains("windows")) {
                // Trabajo para evitar problemas con la carga de las DLL de LWJGL3 en Windows.
                // LWJGL3 extrae por defecto en la carpeta temp del usuario. Si hay caracteres
                // no ASCII, falla.
                // Usamos la carpeta ProgramData como alternativa segura.
                String programData = System.getenv("ProgramData");
                if (programData == null)
                    programData = "C:\\Temp\\";
                String prevTmpDir = System.getProperty("java.io.tmpdir", programData);
                String prevUser = System.getProperty("user.name", "libGDX_User");
                System.setProperty("java.io.tmpdir", programData + "/libGDX-temp");
                System.setProperty("user.name",
                        ("User_" + prevUser.hashCode() + "_GDX" + Version.VERSION).replace('.', '_'));
                Lwjgl3NativesLoader.load();
                System.setProperty("java.io.tmpdir", prevTmpDir);
                System.setProperty("user.name", prevUser);
            }
            return false;
        }

        // No es necesario en GraalVM native image.
        if (!System.getProperty("org.graalvm.nativeimage.imagecode", "").isEmpty()) {
            return false;
        }

        // Comprobamos si ya estamos en el hilo principal (main thread).
        long objc_msgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");
        long NSThread = objc_getClass("NSThread");
        long currentThread = invokePPP(NSThread, sel_getUid("currentThread"), objc_msgSend);
        boolean isMainThread = invokePPZ(currentThread, sel_getUid("isMainThread"), objc_msgSend);
        if (isMainThread)
            return false;

        long pid = LibC.getpid();

        // Comprobamos si -XstartOnFirstThread esta habilitado.
        if ("1".equals(System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid))) {
            return false;
        }

        // Evitamos bucles infinitos de reinicio.
        if ("true".equals(System.getProperty(JVM_RESTARTED_ARG))) {
            System.err.println("Problema al evaluar si la JVM se inicio con -XstartOnFirstThread.");
            return false;
        }

        // Reiniciamos la JVM con el argumento necesario.
        ArrayList<String> jvmArgs = new ArrayList<>();
        String separator = System.getProperty("file.separator", "/");
        String javaExecPath = System.getProperty("java.home") + separator + "bin" + separator + "java";

        if (!(new File(javaExecPath)).exists()) {
            System.err.println("No se encontro la instalacion de Java.");
            return false;
        }

        jvmArgs.add(javaExecPath);
        jvmArgs.add("-XstartOnFirstThread");
        jvmArgs.add("-D" + JVM_RESTARTED_ARG + "=true");
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        jvmArgs.add("-cp");
        jvmArgs.add(System.getProperty("java.class.path"));
        String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
        if (mainClass == null) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            if (trace.length > 0) {
                mainClass = trace[trace.length - 1].getClassName();
            } else {
                System.err.println("No se pudo determinar la clase principal.");
                return false;
            }
        }
        jvmArgs.add(mainClass);

        try {
            if (!redirectOutput) {
                ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
                processBuilder.start();
            } else {
                Process process = (new ProcessBuilder(jvmArgs)).redirectErrorStream(true).start();
                BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = processOutput.readLine()) != null) {
                    System.out.println(line);
                }
                process.waitFor();
            }
        } catch (Exception e) {
            System.err.println("Problema al reiniciar la JVM.");
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Inicia una nueva JVM si es necesario en macOS y redirige la salida.
     *
     * @return true si se inicio una nueva JVM.
     */
    public static boolean startNewJvmIfRequired() {
        return startNewJvmIfRequired(true);
    }
}