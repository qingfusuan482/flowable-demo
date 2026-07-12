package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.model.entity.SysDept;
import org.example.repository.SysDeptMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDeptService {

    private final SysDeptMapper deptMapper;

    public List<SysDept> treeList() {
        List<SysDept> all = deptMapper.selectAll();
        return buildTree(all);
    }

    public List<SysDept> listAll() {
        return deptMapper.selectAll();
    }

    public SysDept getById(Long id) {
        return deptMapper.selectById(id);
    }

    public void save(SysDept dept) {
        deptMapper.insert(dept);
    }

    public void update(SysDept dept) {
        deptMapper.updateById(dept);
    }

    public void delete(Long id) {
        deptMapper.deleteById(id);
    }

    private List<SysDept> buildTree(List<SysDept> list) {
        java.util.Map<Long, SysDept> map = new java.util.HashMap<>();
        java.util.List<SysDept> tree = new java.util.ArrayList<>();

        for (SysDept d : list) {
            map.put(d.getDeptId(), d);
            d.setChildren(null);
        }
        for (SysDept d : list) {
            if (d.getParentId() == null || !map.containsKey(d.getParentId())) {
                tree.add(d);
            } else {
                SysDept parent = map.get(d.getParentId());
                if (parent.getChildren() == null) {
                    parent.setChildren(new java.util.ArrayList<>());
                }
                parent.getChildren().add(d);
            }
        }
        return tree;
    }
}
