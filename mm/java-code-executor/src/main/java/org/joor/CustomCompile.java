/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joor;

/* [java-8] */

// ...

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.joor.Compile.CharSequenceJavaFileObject;
import static org.joor.Compile.ClassFileManager;


/**
 * Based on {@link Compile}
 */
public class CustomCompile {

    public static Class<?> compileAndLoadToCustomClassLoader(String qualifiedClass, String content, CompileOptions compileOptions) {
        ClassLoader cl = MethodHandles.lookup().lookupClass().getClassLoader();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        try {
            ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));

            List<CharSequenceJavaFileObject> files = new ArrayList<CharSequenceJavaFileObject>();
            files.add(new CharSequenceJavaFileObject(qualifiedClass, content));
            StringWriter out = new StringWriter();

            List<String> options = new ArrayList<String>();
            StringBuilder classpath = new StringBuilder();
            String separator = System.getProperty("path.separator");
            String prop = System.getProperty("java.class.path");

            if (prop != null && !"".equals(prop))
                classpath.append(prop);

            if (cl instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) cl).getURLs()) {
                    if (classpath.length() > 0)
                        classpath.append(separator);

                    if ("file".equals(url.getProtocol()))
                        classpath.append(new File(url.getFile()));
                }
            }

            options.addAll(Arrays.asList("-classpath", classpath.toString()));
            CompilationTask task = compiler.getTask(out, fileManager, null, options, null, files);

            if (!compileOptions.processors.isEmpty())
                task.setProcessors(compileOptions.processors);

            task.call();

            if (fileManager.isEmpty())
                throw new ReflectException("Compilation error: " + out);

            Class<?> result = null;

            // This works if we have private-access to the interfaces in the class hierarchy
            if (Reflect.CACHED_LOOKUP_CONSTRUCTOR != null) {
                result = fileManager.loadAndReturnMainClass(qualifiedClass,
                                                            (name, bytes) -> Reflect.on(new CustomClassLoader(cl))
                                                                                    .call("defineClass", name, bytes, 0, bytes.length).get());
            }

            return result;
        } catch (ReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new ReflectException("Error while compiling " + qualifiedClass, e);
        }
    }

    private static class CustomClassLoader extends ClassLoader {

        CustomClassLoader(ClassLoader parent) {
            super(parent);
        }
    }
}
/* [/java-8] */
