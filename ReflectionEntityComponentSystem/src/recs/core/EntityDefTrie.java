package recs.core;

import java.util.Arrays;

import recs.core.utils.RECSIntArray;
import recs.core.utils.RECSIntMap;

/**
 * 
 * @author Enrico van Oosten
 */
public class EntityDefTrie {
    public Node root = new Node();

    public boolean hasDef(EntityDef def) {
        Node node = root;
        for (int id : def.components.items) {
            if (!node.has(id)) {
                return false;
            } else {
                node = node.get(id);
            }
        }
        if (node.def != null)
            return true;
        return false;
    }

    public EntityDef getDef(RECSIntArray componentIds) {
        componentIds.sort();

        Node node = root;
        for (int i = 0; i < componentIds.size; i++) {
            int id = componentIds.items[i];
            node = node.get(id);
            if (node == null)
                return null;
        }
        if(node.def != null) 
            return node.def;
        return null;
    }

    public EntityDef getDef(int[] componentIds) {
        Arrays.sort(componentIds);
        Node node = root;
        for (int id : componentIds) {
            node = node.get(id);
            if (node == null)
                return null;
        }
        if (node.def != null)
            return node.def;
        return null;
    }

    public void insert(EntityDef def) {
        def.components.sort();
        Node node = root;
        for (int id : def.components.items) {
            if (node.has(id)) {
                node = node.get(id);
            } else {
                node = node.insert(id);
            }
        }
        node.def = def;
    }

    private class Node {
        public EntityDef def = null;
        public RECSIntMap<Node> childNodes;

        public boolean has(int id) {
            if (childNodes == null) {
                childNodes = new RECSIntMap<Node>(3);
                return false;
            }
            return childNodes.containsKey(id);
        }

        public Node get(int id) {
            return childNodes.get(id);
        }

        public Node insert(int id) {
            Node n = new Node();
            childNodes.put(id, n);
            return n;
        }
    }
}
