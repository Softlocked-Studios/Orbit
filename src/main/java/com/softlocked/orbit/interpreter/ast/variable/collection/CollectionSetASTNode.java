package com.softlocked.orbit.interpreter.ast.variable.collection;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.datatypes.classes.OrbitObject;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionSetASTNode implements ASTNode {
    public ASTNode collection;
    public List<ASTNode> indices;
    public ASTNode value;

    public CollectionSetASTNode(ASTNode collection, List<ASTNode> indices, ASTNode value) {
        this.collection = collection;
        this.indices = indices;
        this.value = value;
    }

    @Override
    public Object evaluate(ILocalContext context) {
        Object collection = this.collection.evaluate(context);
        List<Object> indices = this.indices.stream().map(index -> index.evaluate(context)).collect(Collectors.toList());
        Object value = this.value.evaluate(context);

        if (collection instanceof List) {
            List<Object> list = (List<Object>) collection;
            int depth = indices.size();
            while (depth > 1) {
                list = (List<Object>) list.get((int) Utils.cast(indices.get(indices.size() - depth), Integer.class));
                depth--;
            }
            list.set((int) Utils.cast(indices.get(indices.size() - 1), Integer.class), value);
        } else if (collection instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) collection;
            int depth = indices.size();
            while (depth > 1) {
                Object key = indices.get(indices.size() - depth);
                if (!map.containsKey(key)) {
                    throw new RuntimeException("Key not found in map");
                }
                map = (Map<Object, Object>) map.get(key);
                depth--;
            }
            map.put(indices.get(indices.size() - 1), value);
        } else if (collection instanceof String string) {
            char[] chars = string.toCharArray();

            if (indices.size() == 1) {
                int index = (int) Utils.cast(indices.get(0), Integer.class);

                if (index < 0 || index >= chars.length) {
                    throw new RuntimeException("Index out of bounds for string access");
                }
                chars[(int) indices.get(0)] = (char) Utils.cast(value, Character.class);
            } else {
                throw new RuntimeException("Invalid number of indices for string access");
            }


        } else if (collection instanceof OrbitObject obj) {
            obj.callFunction("collection.set", List.of(indices, value));
        } else {
            throw new RuntimeException("Invalid collection type for access");
        }

        return value;
    }

    @Override
    public String toString() {
        return "CollectionSetASTNode{" +
                "collection=" + collection +
                ", indices=" + indices +
                ", value=" + value +
                '}';
    }
}