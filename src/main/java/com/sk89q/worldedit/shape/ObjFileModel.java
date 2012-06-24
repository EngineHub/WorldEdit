package com.sk89q.worldedit.shape;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.RegionOperationException;

public class ObjFileModel extends AbstractModel {
    public ObjFileModel(String path, int geometryIndex) throws IOException, RegionOperationException {
        super(load(path, geometryIndex));
    }

    protected static List<Vertex> load(String path, int geometryIndex) throws IOException, RegionOperationException {
        FileInputStream stream = new FileInputStream(path);
        Scanner scanner = new Scanner(stream);
        //Set up all variables used
        List<Vector> V = new ArrayList<Vector>();
        List<Vector> VN = new ArrayList<Vector>();
        List<Vector> VT = new ArrayList<Vector>();
        //java.util.Vector<Vertex> F = new java.util.Vector<Vertex> ();
        //java.util.Vector<Material> M = new java.util.Vector<Material> ();
        int currentGeometry = -1;
        while (scanner.hasNextLine()){
            //parse and prepare a new line to the components
            String line = scanner.nextLine().replace(",", ".").replace("/", " ").replace("\\", " ").toLowerCase().trim();
            while (line.contains("  ")) line = line.replace("  ", " ");
            String[] components = line.split(" ");
            //parse components
            if (components.length != 0) {
                if (components[0].startsWith("vt")) {
                    //vt U V
                    //vt 0,303648 0,857611
                    float u = Float.parseFloat(components[1]);
                    float v = Float.parseFloat(components[2]);
                    VT.add(new Vector(u, v, 0));
                } else if (components[0].startsWith("vn")) {
                    //vn nX nY nZ
                    //vn -0,855562 -0,415001 0,309496
                    float nx = Float.parseFloat(components[1]);
                    float ny = Float.parseFloat(components[2]);
                    float nz = Float.parseFloat(components[3]);
                    VN.add(new Vector(nx, ny, nz));
                } else if (components[0].startsWith("v")) {
                    //v x y z
                    //v -3,203888 -4,903336 4,873919
                    float x = Float.parseFloat(components[1]);
                    float y = Float.parseFloat(components[2]);
                    float z = Float.parseFloat(components[3]);
                    V.add(new Vector(x, y, z));
                    /*
                } else if (components[0].startsWith("f")) {
                    //f 9105/9105/9105 9107/9107/9107 9104/9104/9104
                    //f V1 VT1 VN1 V2 VT2 VN2 V3 VT3 VN3
                    //f stands for "face", after this command 3x the indices to V/VT/VN to complete a Vertex

                    //Check if faces should be loaded (geometry index)
                    if (currentgeom == geometryindex) {

                        //Convert the indices after the 'f' to actual integers
                        int[] indices = new int[components.length - 1];
                        for (int i=0;i<indices.length;i++) {
                            indices[i] = Integer.parseInt(components[i + 1]);
                        }
                        //Check vertex attributes
                        int attrlength = indices.length / 3; //length of a single attribute list for a single vertex
                        boolean hasUV = (VT.size() != 0);
                        boolean hasNormals = (VN.size() != 0);
                        //Apply values to vertices and add to list
                        for (int vi=0;vi<3;vi++) {
                            Vertex v = new Vertex(false, hasNormals, hasUV);

                            int vindex = indices[vi * attrlength] - 1;
                            v.setPosition(V.get(vindex));
                            if (hasUV) {
                                int vtindex = indices[vi * attrlength + 1] - 1;
                                v.setUV(VT.get(vtindex));
                            }
                            if (hasNormals) {
                                int vnindex = indices[vi * attrlength + 1] - 1;
                                if (hasUV) vnindex += 1;
                                v.setNormal(VT.get(vnindex));
                            }
                            F.add(v);
                        }
                    }
                    */
                } else if (components[0].startsWith("g")) {
                    //Stop if the last loaded geometry was the asked-for-geometry
                    if (currentGeometry == geometryIndex/* && F.size() != 0*/) break;
                    ++currentGeometry;
                    /*
                } else if (components[0].startsWith("usemtl")) {
                    //update previous material
                    if (F.size() != 0) {
                        M.get(M.size() - 1).vertices = new Vertex[F.size()];
                        for (int i=0;i<F.size();i++) M.get(M.size() - 1).vertices[i] = F.get(i);
                        F.clear();
                    }
                    M.add(new Material());
                    */
                }
            }
        }
        scanner.close();
        stream.close();
        /*
        //Add data if it remains
        if (F.size() != 0) {
            M.get(M.size() - 1).vertices = new Vertex[F.size()];
            for (int i=0;i<F.size();i++) M.get(M.size() - 1).vertices[i] = F.get(i);
            F.clear();
        }

        //Set self
        this.materials = new Material[M.size()];
        for (int mi=0;mi<M.size();mi++) {
            this.materials[mi] = new Material(M.get(mi).vertices);
        }
        this.generatePoints();
        */

        final Map<Vector, List<Integer>> indexLists = new LinkedHashMap<Vector, List<Integer>>();
        if (V.size() != VN.size()) {
            throw new RegionOperationException(String.format("The amounts of positions(%d) and normals(%d) do not match!\n", V.size(), VN.size()));
        }
        for (int i = 0; i < Math.min(V.size(), VN.size()); ++i) {
            final Vector position = V.get(i);
            List<Integer> indexList = indexLists.get(position);
            if (indexList == null) {
                indexLists.put(position, indexList = new ArrayList<Integer>());
            }

            indexList.add(i);
        }

        final List<Vertex> vertices = new ArrayList<Vertex>();
        for (Map.Entry<Vector, List<Integer>> entry : indexLists.entrySet()) {
            final Vector position = entry.getKey();

            final List<Integer> indexList = entry.getValue();
            assert !indexList.isEmpty();

            if (indexList.size() == 1) {
                vertices.add(new Vertex(position, VN.get(indexList.get(0))));
                continue;
            }

            final Vector[] normals = new Vector[indexList.size()];
            int i = 0;
            for (int index : indexList) {
                normals[i++] = VN.get(index);
            }
            vertices.add(new MultiVertex(position, normals));
        }
        return vertices;
    }
}
