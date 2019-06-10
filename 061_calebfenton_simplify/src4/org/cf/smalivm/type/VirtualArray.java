package org.cf.smalivm.type;

import org.cf.smalivm.dex.CommonTypes;
import org.cf.util.ClassNameUtils;
import org.jf.dexlib2.iface.reference.TypeReference;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Arrays have a clone method and a length field. They're both generated by Java on-demand.
 * https://stackoverflow.com/questions/38223880/how-did-jvm-implement-arrays-class
 */
public class VirtualArray extends VirtualType {

    private final int dimensionRank;
    private Set<VirtualType> ancestors;
    private Map<String, VirtualArrayMethod> methodDescriptorToMethod;

    VirtualArray(TypeReference typeReference) {
        super(typeReference, typeReference.getType(), ClassNameUtils.internalToBinary(typeReference.getType()),
              ClassNameUtils.internalToSource(typeReference.getType()));
        dimensionRank = ClassNameUtils.getDimensionCount(typeReference.getType());
    }

    private static String buildRankString(int rank) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rank; i++) {
            sb.append('[');
        }

        return sb.toString();
    }

    private static void getAncestors0(VirtualType virtualType, Set<VirtualType> ancestors) {
        if (ancestors.contains(virtualType)) {
            return;
        }
        ancestors.add(virtualType);

        for (VirtualType ancestor : virtualType.getImmediateAncestors()) {
            getAncestors0(ancestor, ancestors);
        }
    }

    @Override
    public Set<? extends VirtualType> getImmediateAncestors() {
        Set<VirtualType> parents = new HashSet<>();
        VirtualType componentType = getComponentType();
        if (componentType.isPrimitive()) {
            String objectType = buildRankString(dimensionRank - 1) + CommonTypes.OBJECT;
            VirtualType parent = classManager.getVirtualType(objectType);
            parents.add(parent);
        } else {
            // Immediate ancestors include arrays of immediate ancestors of the same rank
            for (VirtualType ancestorComponent : componentType.getImmediateAncestors()) {
                String ancestorName = '[' + ancestorComponent.getTypeReference().getType();
                VirtualArray parent = (VirtualArray) classManager.getVirtualType(ancestorName);
                parents.add(parent);
            }

            if (componentType.getName().endsWith(CommonTypes.OBJECT)) {
                parents.add(componentType);
            }
        }

        return parents;
    }

    @Override
    public Set<? extends VirtualType> getAncestors() {
        if (ancestors != null) {
            return ancestors;
        }

        ancestors = new LinkedHashSet<>();
        getAncestors0(this, ancestors);
        ancestors.remove(this);

        return ancestors;
    }

    @Override
    public VirtualField getField(String fieldName) {
        return null;
    }

    @Override
    public Collection<VirtualField> getFields() {
        // someArray[].length is actually handled by array-length op
        return Collections.emptyList();
    }

    @Override
    public VirtualMethod getMethod(String methodDescriptor) {
        VirtualMethod method = getMethod0(methodDescriptor);
        if (method != null) {
            return method;
        }
        for (VirtualType ancestor : getAncestors()) {
            if (ancestor instanceof VirtualArray) {
                method = ((VirtualArray) ancestor).getMethod0(methodDescriptor);
                if (method != null) {
                    return method;
                }
            }
        }

        return null;
    }

    @Override
    public Collection<VirtualMethod> getMethods() {
        if (methodDescriptorToMethod == null) {
            methodDescriptorToMethod = buildMethodsMap();
        }
        List<VirtualMethod> methods = new LinkedList<>();
        methods.addAll(methodDescriptorToMethod.values());

        return methods;
    }

    @Override
    public boolean instanceOf(VirtualType targetType) {
        if (equals(targetType)) {
            return true;
        }
        for (VirtualType ancestor : getAncestors()) {
            if (ancestor.equals(targetType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    public VirtualType getBaseType() {
        String baseType = ClassNameUtils.getComponentBase(getName());

        return classManager.getVirtualType(baseType);
    }

    public VirtualType getComponentType() {
        String componentType = ClassNameUtils.getComponentType(getName());

        return classManager.getVirtualType(componentType);
    }

    private VirtualArrayMethod getMethod0(String methodDescriptor) {
        if (methodDescriptorToMethod == null) {
            methodDescriptorToMethod = buildMethodsMap();
        }

        return methodDescriptorToMethod.get(methodDescriptor);
    }

    private Map<String, VirtualArrayMethod> buildMethodsMap() {
        Map<String, VirtualArrayMethod> methods = new HashMap<>(2);
        String methodDescriptor = "clone()Ljava/lang/Object;";
        String methodSignature = getName() + "->" + methodDescriptor;
        TypeReference reference = classManager.getFrameworkDexBuilder().internTypeReference(methodSignature);

        VirtualArrayMethod method = new VirtualArrayMethod(reference, this);
        methods.put(methodDescriptor, method);

        return methods;
    }

}