/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.report;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SystemInfoReport extends DataReport {

    public SystemInfoReport() {
        super("System Information");

        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        append("Java", "%s %s (%s)",
                System.getProperty("java.vendor"),
                System.getProperty("java.version"),
                System.getProperty("java.vendor.url"));
        append("Operating System", "%s %s (%s)",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        append("Available Processors", runtime.availableProcessors());
        append("Free Memory", runtime.freeMemory() / 1024 / 1024 + " MB");
        append("Max Memory", runtime.maxMemory() / 1024 / 1024 + " MB");
        append("Total Memory", runtime.totalMemory() / 1024 / 1024 + " MB");
        append("System Load Average", osBean.getSystemLoadAverage());
        append("Java Uptime", TimeUnit.MINUTES.convert(runtimeBean.getUptime(), TimeUnit.MILLISECONDS) + " minutes");

        DataReport startup = new DataReport("Startup");
        startup.append("Input Arguments", runtimeBean.getInputArguments());
        append(startup.getTitle(), startup);

        DataReport vm = new DataReport("Virtual Machine");
        vm.append("Name", runtimeBean.getVmName());
        vm.append("Vendor", runtimeBean.getVmVendor());
        vm.append("Version", runtimeBean.getVmVendor());
        append(vm.getTitle(), vm);

        DataReport spec = new DataReport("Specification");
        spec.append("Name", runtimeBean.getSpecName());
        spec.append("Vendor", runtimeBean.getSpecVendor());
        spec.append("Version", runtimeBean.getSpecVersion());
        append(spec.getTitle(), spec);

        DataReport classLoader = new DataReport("Class Loader");
        classLoader.append("Loaded Class Count", classLoadingBean.getLoadedClassCount());
        classLoader.append("Total Loaded Class Count", classLoadingBean.getTotalLoadedClassCount());
        classLoader.append("Unloaded Class Count", classLoadingBean.getUnloadedClassCount());
        append(classLoader.getTitle(), classLoader);

        DataReport gc = new DataReport("Garbage Collectors");
        for (GarbageCollectorMXBean bean : gcBeans) {
            DataReport thisGC = new DataReport(bean.getName());
            thisGC.append("Collection Count", bean.getCollectionCount());
            thisGC.append("Collection Time", bean.getCollectionTime() + "ms");
            gc.append(thisGC.getTitle(), thisGC);
        }
        append(gc.getTitle(), gc);

        DataReport threads = new DataReport("Threads");
        for (ThreadInfo threadInfo : threadBean.dumpAllThreads(false, false)) {
            threads.append("#" + threadInfo.getThreadId() + " " + threadInfo.getThreadName(), threadInfo.getThreadState());
        }
        append(threads.getTitle(), threads);
    }

}
