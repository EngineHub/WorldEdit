// $Id$
/*
 * Registrar
 * Copyright (C) 2011 Charles Hymes <http://www.hymerfania.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.sk89q.worldedit;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.bukkit.BukkitServerInterface;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Registers all of the commands, in all of the jars, in the WorldEdit directory.
 * <p>
 * Usage: Obtain a jar that has a class or classes that has methods annotated
 * with the <code>Command</code>  <code>Annotation</code>.<br/>
 * Then place the jar in the 
 *  <code>"plugins/WorldEdit"</code> directory of your Bukkit server installation.<br/>
 * Restart your Bukkit server.<br/> That's it!<br/>
 * <p>
 * Developer Notes:
 * <p>
 * Also, the available <code>Logger</code> does not seem to  support the 
 * default Loggers formatting functionality, so logs concatenate strings instead
 * of using <code>{}</code>.
 * @param <T> command sender class
 * @author charles@hymes.name
 * @see com.sk89q.minecraft.util.commands.Command
 **/
public class Registrar<T> {

    private static class ClasspathJarAppender {

        protected ClasspathJarAppender() {
        }

        /**
         * Adds a file to the classpath.
         * @param s a String pointing to the file
         * @throws IOException
         */
        public static void addFile(String s) throws IOException {
            File f = new File(s);
            addFile(f);
        }

        /**
         * Adds a file to the classpath
         * @param f the file to be added
         * @throws IOException
         */
        public static void addFile(File f) throws IOException {
            LOGGER.log(Level.FINE, f.toString());
            addURL(f.toURI().toURL());
        }

        /**
         * Adds the content pointed by the URL to the classpath.
         * @param u the URL pointing to the content to be added
         * @throws IOException
         */
        public static void addURL(URL u) throws IOException {
            LOGGER.log(Level.FINE, " adding " + u.toString() + " to classpath");
            Class<URLClassLoader> urlClassLoaderClass = URLClassLoader.class;
            try {
                Method method = urlClassLoaderClass.getDeclaredMethod("addURL", URL_PARAMETER);
                method.setAccessible(true);
                method.invoke(CLASS_LOADER, new Object[]{u});
                LOGGER.log(Level.FINE, "Successfully added " + u.toString() + " to classpath");
            }
            /**May have different handling of these exceptions someday***/
            catch (IllegalAccessException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new IOException(FAIL_PREFIX + u.toString() + FAIL_SUFFIX, ex);
            }
            catch (IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new IOException(FAIL_PREFIX + u.toString() + FAIL_SUFFIX, ex);
            }
            catch (InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new IOException(FAIL_PREFIX + u.toString() + FAIL_SUFFIX, ex);
            }
            catch (NoSuchMethodException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new IOException(FAIL_PREFIX + u.toString() + FAIL_SUFFIX, ex);
            }
            catch (SecurityException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new IOException(FAIL_PREFIX + u.toString() + FAIL_SUFFIX, ex);
            }
            catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new IOException(FAIL_PREFIX + u.toString() + FAIL_SUFFIX, ex);
            }            
            catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new IOException(FAIL_PREFIX + u.toString() + FAIL_SUFFIX, ex);
            }
        }

        protected Class<?> findClass(String name) throws ClassNotFoundException {
            LOGGER.log(Level.FINE, "finding and loading class " + name);
            Class<?> result = null;
            try{
                result = Class.forName(name);
            }
            catch(ClassNotFoundException ncfe){
                 result = null;
            }
            if(result == null){
                            
                Class<URLClassLoader> urlClassLoaderClass = URLClassLoader.class;
                try {
                    Method method = urlClassLoaderClass.getDeclaredMethod("findClass", CLASSNAME_PARAMETER);
                    method.setAccessible(true);
                    result = (Class<?>) method.invoke(CLASS_LOADER, new Object[]{name});
                    LOGGER.log(Level.FINE, "Successfully found and loaded class " + name);
                }
                /**May have different handling of these exceptions someday***/
                catch (IllegalAccessException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    throw new ClassNotFoundException(FAIL_PREFIX + name + FAIL_SUFFIX, ex);
                }
                catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    throw new ClassNotFoundException(FAIL_PREFIX + name + FAIL_SUFFIX, ex);
                }
                catch (InvocationTargetException ex) {
                    if(ex.getCause() instanceof NoClassDefFoundError){
                        NoClassDefFoundError ncdf = (NoClassDefFoundError)ex.getCause();
                        throw new ClassNotFoundException(FAIL_PREFIX + name + FAIL_SUFFIX, ncdf);/***Breaks here **/                    
                    }
                    else{
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    throw new ClassNotFoundException(FAIL_PREFIX + name + FAIL_SUFFIX, ex);
                    }
                }
                catch (NoSuchMethodException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    throw new ClassNotFoundException(FAIL_PREFIX + name + FAIL_SUFFIX, ex);
                }
                catch (SecurityException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    throw new ClassNotFoundException(FAIL_PREFIX + name + FAIL_SUFFIX, ex);
                }
                catch (RuntimeException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    throw new ClassNotFoundException(FAIL_PREFIX + name + FAIL_SUFFIX, ex);
                }            
                catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    throw new ClassNotFoundException(FAIL_PREFIX + name + FAIL_SUFFIX, ex);
                }
            }
            return result;
        }
        /**
         * Parameters of the method to add an URL to the System classes. 
         */
        private static final Class<?>[] URL_PARAMETER = new Class<?>[]{URL.class};
        private static final Class<?>[] CLASSNAME_PARAMETER = new Class<?>[]{String.class};
        private static final URLClassLoader CLASS_LOADER = (URLClassLoader) Registrar.class.getClassLoader();
        private static final String FAIL_PREFIX = "Error, could not load class from URL ";
        private static final String FAIL_SUFFIX = " via local ClassLoader";
    }

    /***
     * Accepts any file that is a directory. Accepts Symbolic links to dirs as well 
     * in Windows NT+. Can only be fixed in Java 1.7
     ***/
    private static class IsDirFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {

        @Override
        public String getDescription() {
            return "Files that are jars";
        }

        @Override
        public boolean accept(File pathname) {
            return accept(pathname, null);
        }

        public boolean accept(File pathname, String dummy) {
            boolean result;
            result = pathname.isDirectory();
            /**This gets more useful and more complicated in JVM 1.7**/
            return result;
        }
    }

    /***
     * Accepts any file that is a plain,regular file. Makes crude attempt to reject 
     * symbolic links. Often fails to detect links in Windows NT+
     * Can only be fixed in Java 1.7
     ***/
    private static class IsFileFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {

        @Override
        public String getDescription() {
            return "Files that are files, not directories or links.";
        }

        /*****
         * @param file
         * @return {@code true} if the file is a symbolic link. However this often fails to 
         * correctly return true on JVMs before 1.7, especially on Windows.
         * @throws IOException  
         * @deprecated There is no good way to do this on Windows without a 1.7 JVM
         */
        @Deprecated
        public static boolean isSymlink(File file) throws IOException {
            boolean isSymbolicLink;

            if (file == null) {
                throw new IllegalArgumentException("File must not be null");
            }
//        java.nio.file.attribute.BasicFileAttributes attrs = Attributes.readBasicFileAttributes(file.toPath());
//        java.nio.file.attribute.BasicFileAttributes attrs;
//        attrs = Files.readAttributes(file.toPath(),BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
//        isSymbolicLink = attrs.isSymbolicLink();
            else {
                File canon;
                if (file.getParent() == null) {
                    canon = file;
                }
                else {
                    File canonDir = file.getParentFile().getCanonicalFile();
                    canon = new File(canonDir, file.getName());
                }
                isSymbolicLink = !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
            }

            if (isSymbolicLink) {
                LOGGER.log(Level.WARNING, file.getPath() + " is a symbolicLink");
                /** Logger does not support {} **/
            }
            return isSymbolicLink;
        }

        @Override
        public boolean accept(File pathname) {
            return accept(pathname, null);
        }

        public boolean accept(File pathname, String dummy) {
            boolean result = false;

            try {
                result = pathname.isFile();
                if (!result) {
                    LOGGER.log(Level.FINEST, pathname.getPath() + " is not a file ");
                    /** Logger does not support {} **/
                }
                result = result && !isSymlink(pathname);
            }
            catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }

            return result;
        }
    }

    /***
     * Accepts any file that is a file, and ends in ".jar"
     * File does not have to exist, nor be readable, etc.
     ***/
    private static class IsJarFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {

        @Override
        public String getDescription() {
            return "Directories, not files.";
        }

        @Override
        public boolean accept(File pathname) {
            return accept(pathname, null);
        }

        public boolean accept(File pathname, String dummy) {
            boolean result;
            result = FILE_FILTER.accept(pathname);
            result = result && pathname.getName().endsWith(".jar");
            return result;
        }
    }

    /**
     * Creates an instance of Registrar. Only needed in special cases, because
     * the static method
     * {@code autoRegister(BukkitServerInterface serverInterface, CommandsManager<LocalPlayer> commandManager)}
     * will typically be easier to use.
     * @param commandManager the CommandsManager that will register the command.
     * @param extenensionsDir the location of the jars that contain the classes
     * with {@code Command}s.
     * @see autoRegister(BukkitServerInterface, CommandsManager)
     */
    public Registrar(CommandsManager<T> commandManager, File extenensionsDir) {
        this._commandManager = commandManager;
        this._extenensionsDir = extenensionsDir;
    }

    /**
     * Registers all of the commands, in all of the classes, in all of the jars, in 
     * all of all of the directories and subdirectories in the WorldEdit directory.
     */
    public void registerExtensionCommands() {
        LOGGER.log(Level.FINE, getExtenensionsDir().toString());
        registerExtensionCommandsInDir(getExtenensionsDir());
    }

    private void registerExtensionCommandsInDir(File extDir) {
        LOGGER.log(Level.FINE, extDir.toString());

        if (!DIR_FILTER.accept(extDir)) {
            throw new IllegalArgumentException("File " + extDir.toString() + " is not a directory.");
        }
        for (File someJar : extDir.listFiles(new IsJarFilter())) {
            registerExtensionCommandsInJar(someJar);
        }
        for (File aSubDir : extDir.listFiles(DIR_FILTER)) {
            registerExtensionCommandsInDir(aSubDir);
        }
    }

    private void registerExtensionCommandsInJar(File someJar) {
        String entryName;
        Class<?> entryClass;
        LOGGER.log(Level.FINE, someJar.toString());

        if (!JAR_FILTER.accept(someJar)) {
            throw new IllegalArgumentException("File " + someJar.toString() + " is not a jar file.");
        }
        try {

            JarFile jarfile;
            ClasspathJarAppender.addFile(someJar);
            jarfile = new JarFile(someJar);
            for (JarEntry someZipEntry : Collections.list(jarfile.entries())) {
                entryName = someZipEntry.getName();
                if (entryName.endsWith(".class") && !entryName.contains("$")) {
                    LOGGER.log(Level.FINE, "Found class entry " + entryName.toString());
                    /**Logger does not support {} formatting**/
                    entryClass = classFromName(entryName);
                    if (entryClass != null) {
                        if (hasCommands(entryClass,0)) {
                            LOGGER.log(Level.INFO, "Registering " + entryClass.getName() + " as Command");
                            getCommandManager().register(entryClass);
                        }
                        else {
                            LOGGER.log(Level.FINE, entryName.toString() + " is not annotated as a Command");
                        }
                    }
                    else {
                        LOGGER.log(Level.FINE, "Could not load " + entryName.toString());
                    }
                }
            }
        }
        catch (ZipException ze) {
            LOGGER.log(Level.SEVERE, null, ze);
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private Class<?> classFromName(String entryName) {
        Class<?> someclass = null;
        String plainName = entryName.substring(0, entryName.length() - 6).replace("/", ".");
        LOGGER.log(Level.FINE, "Searching for class " + plainName);
        /**Logger does not support {} formatting**/
        try {
            someclass = _loaderStub.findClass(plainName);
        }
        catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            someclass = null;
        }
        catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            someclass = null;
        }        
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            someclass = null;
        }

        return someclass;
    }

    private boolean hasCommands(final Class<?> someClass, final int depth) {
        boolean result = false;
        Method[] classMethods = null;
        Class<?>[] innerClasses = null;
        if(depth > MAX_DEPTH){
            return false;
        }
        try {
            classMethods = someClass.getMethods();
            for (Method aMethod : classMethods) {
                boolean isCommand = false;
                try {
                    isCommand = aMethod.isAnnotationPresent(Command.class);
                }
                catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
                if (isCommand) {
                    result = true;
                    break;
                }
            }
        }
        catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        if (!result && (depth < MAX_DEPTH)) {
            try {
                innerClasses = someClass.getClasses();
                for (Class<?> innerClass : innerClasses) {
                    if ( hasCommands(innerClass, depth+1)) {
                        result = true;
                        break;
                    }
                }
            }
            /**Can't use Throwable,it enables the StackOverFlowError catastrophe**/
            catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return result;
    }

/***
 * Registers all of the commands, in all of the classes, in all of the jars, in 
 * all of all of the directories and subdirectories in the WorldEdit directory.
 * <p>
 * This is a convenience wrapper for {@code  autoRegister(BukkitServerInterface server, CommandsManager<LocalPlayer> commandManager)}
 * Note, however, that the argument {@code server} must be an instance of {@code BukkitServerInterface}.
 * @param serverInterface The BukkitServerInterface that manages the WorldEdit plugin.
 * @param commandManager the CommandsManager that will register the command. 
 */    
    public static void autoRegister(ServerInterface serverInterface, CommandsManager<LocalPlayer> commandManager) {
        if (!(serverInterface instanceof BukkitServerInterface)) {
            throw new IllegalArgumentException("server argument must be an instance of BukkitServerInterface");
        }
        autoRegister((BukkitServerInterface)serverInterface,commandManager);
    }
/***
 * Registers all of the commands, in all of the classes, in all of the jars, in 
 * all of all of the directories and subdirectories in the WorldEdit directory.
 * <p>
 * @param serverInterface The BukkitServerInterface that manages the WorldEdit plugin.
 * @param commandManager   
 */
    public static void autoRegister(BukkitServerInterface serverInterface, CommandsManager<LocalPlayer> commandManager) {
        try {
            File wcPluginDir = serverInterface.plugin.getDataFolder();
            LOGGER.log(Level.INFO, "Searching directory \"" + wcPluginDir.toString() + "\" for Commands");
            /** Logger does not support {} **/
            if (wcPluginDir.exists()) {
                Registrar<LocalPlayer> jarRegistrar = new Registrar<LocalPlayer>(commandManager, wcPluginDir);
                jarRegistrar.registerExtensionCommands();
            }
            else {
                LOGGER.log(Level.WARNING, "Plugin directory \"" + wcPluginDir.toString() + "\" does not (yet) exist.");
                /** Logger does not support {} **/
            }
        }
        catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * @return the commandManager
     */
    public CommandsManager<T> getCommandManager() {
        return _commandManager;
    }

    /**
     * @param commandManager the commandManager to set
     */
    public final void setCommandManager(CommandsManager<T> commandManager) {
        this._commandManager = commandManager;
    }

    /**
     * @return the _extenensionsDir
     */
    public File getExtenensionsDir() {
        return _extenensionsDir;
    }

    /**
     * @param extenensionsDir the _extenensionsDir to set
     */
    public final void setExtenensionsDir(File extenensionsDir) {
        this._extenensionsDir = extenensionsDir;
    }
    private CommandsManager<T> _commandManager;
    private File _extenensionsDir = null;
    private static final Logger LOGGER = Logger.getLogger("Minecraft.WorldEdit");
    private static final IsDirFilter DIR_FILTER = new IsDirFilter();
    private static final IsFileFilter FILE_FILTER = new IsFileFilter();
    private static final IsJarFilter JAR_FILTER = new IsJarFilter();
    private final ClasspathJarAppender _loaderStub = new ClasspathJarAppender();
    public static final int MAX_DEPTH = 5;
}
