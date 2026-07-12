package org.example.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TreeUtils {

    /**
     * 将平级列表转为树形结构
     * @param list       平级列表
     * @param getId      获取ID的函数
     * @param getPid     获取父ID的函数
     * @param getChildren 获取子节点列表的方法引用
     * @param <T>        节点类型
     */
    public static <T> List<T> buildTree(List<T> list,
                                         Function<T, ?> getId,
                                         Function<T, ?> getPid,
                                         Function<T, List<T>> getChildren) {
        if (list == null || list.isEmpty()) return new ArrayList<>();

        Map<?, T> nodeMap = list.stream().collect(Collectors.toMap(getId, Function.identity(), (a, b) -> a));
        List<T> tree = new ArrayList<>();

        for (T node : list) {
            Object pid = getPid.apply(node);
            if (pid == null || !nodeMap.containsKey(pid)) {
                tree.add(node);
            } else {
                T parent = nodeMap.get(pid);
                List<T> children = getChildren.apply(parent);
                if (children == null) {
                    // 子列表不存在，跳过（可通过反射set，这里用更简单的方式）
                    continue;
                }
                children.add(node);
            }
        }
        return tree;
    }
}
