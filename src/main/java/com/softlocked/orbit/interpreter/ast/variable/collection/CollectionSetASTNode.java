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
            if (indices.size() == 1) {
                map.put(indices.get(0), value);
            } else {
                throw new RuntimeException("Invalid number of indices for map access");
            }
        } else if (collection instanceof OrbitObject obj) {
            obj.callFunction("[]=", List.of(indices, value));
        } else {
            throw new RuntimeException("Invalid collection type for access");
        }

        return value;
    }
}