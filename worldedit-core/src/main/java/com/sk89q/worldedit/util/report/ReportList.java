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

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReportList implements Report, List<Report> {

    private final String title;
    private final List<Report> reports = Lists.newArrayList();

    public ReportList(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int size() {
        return reports.size();
    }

    @Override
    public boolean isEmpty() {
        return reports.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return reports.contains(o);
    }

    @Override
    public Iterator<Report> iterator() {
        return reports.iterator();
    }

    @Override
    public Object[] toArray() {
        return reports.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return reports.toArray(a);
    }

    @Override
    public boolean add(Report report) {
        return reports.add(report);
    }

    @Override
    public boolean remove(Object o) {
        return reports.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return reports.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Report> c) {
        return reports.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Report> c) {
        return reports.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return reports.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return reports.retainAll(c);
    }

    @Override
    public void clear() {
        reports.clear();
    }

    @Override
    public boolean equals(Object o) {
        return reports.equals(o);
    }

    @Override
    public int hashCode() {
        return reports.hashCode();
    }

    @Override
    public Report get(int index) {
        return reports.get(index);
    }

    @Override
    public Report set(int index, Report element) {
        return reports.set(index, element);
    }

    @Override
    public void add(int index, Report element) {
        reports.add(index, element);
    }

    @Override
    public Report remove(int index) {
        return reports.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return reports.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return reports.lastIndexOf(o);
    }

    @Override
    public ListIterator<Report> listIterator() {
        return reports.listIterator();
    }

    @Override
    public ListIterator<Report> listIterator(int index) {
        return reports.listIterator(index);
    }

    @Override
    public List<Report> subList(int fromIndex, int toIndex) {
        return reports.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        if (!reports.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Report report : reports) {
                builder.append("================================\n")
                        .append(report.getTitle())
                        .append("\n================================")
                        .append("\n\n")
                        .append(report.toString())
                        .append("\n\n");
            }
            return builder.toString();
        } else {
            return "No reports.";
        }
    }

}
