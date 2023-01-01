/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 * Copyright (C) miguelgargallo to Optimize and improve the code
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

 package com.sk89q.worldedit.cli;

 import java.util.List;
 
 public interface CLIWorld {
 
     /**
      * Saves this world to the specified file if dirty or forced.
      *
      * @param force Force a save
      * @param filePath The file path to save to
      */
     void save(boolean force, String filePath);
 
     /**
      * Saves the specified changes to this world to the specified file if dirty or forced.
      *
      * @param force Force a save
      * @param filePath The file path to save to
      * @param changes The changes to save
      */
     void save(boolean force, String filePath, List<Change> changes);
 
     /**
      * Performs a dirty check and saves this world to the specified file if necessary.
      *
      * @param filePath The file path to save to
      */
     void saveIfDirty(String filePath);
 
     /**
      * Performs a dirty check and saves the specified changes to this world to the specified file if necessary.
      *
      * @param filePath The file path to save to
      * @param changes The changes to save
      */
     void saveIfDirty(String filePath, List<Change> changes);
 
     /**
      * Gets whether the world is dirty.
      *
      * @return If it's dirty
      */
     boolean isDirty();
 
     /**
      * Set the world's dirty status.
      *
      * @param dirty if dirty
      */
     void setDirty(boolean dirty);
 }
 