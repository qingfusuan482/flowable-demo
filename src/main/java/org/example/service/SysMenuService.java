package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.model.entity.SysMenu;
import org.example.repository.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;

    public List<SysMenu> treeList() {
        List<SysMenu> all = menuMapper.selectAll();
        return buildTree(all);
    }

    public List<SysMenu> listAll() {
        return menuMapper.selectAll();
    }

    public SysMenu getById(Long id) {
        return menuMapper.selectById(id);
    }

    public void save(SysMenu menu) {
        menuMapper.insert(menu);
    }

    public void update(SysMenu menu) {
        menuMapper.updateById(menu);
    }

    public void delete(Long id) {
        menuMapper.deleteById(id);
        // 同时删除子菜单
        deleteChildren(id);
    }

    private void deleteChildren(Long parentId) {
        List<SysMenu> children = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, parentId));
        for (SysMenu child : children) {
            delete(child.getMenuId());
        }
    }

    private List<SysMenu> buildTree(List<SysMenu> list) {
        Map<Long, SysMenu> map = new HashMap<>();
        List<SysMenu> tree = new ArrayList<>();

        for (SysMenu m : list) {
            map.put(m.getMenuId(), m);
            m.setChildren(null);
        }
        for (SysMenu m : list) {
            if (m.getParentId() == null || !map.containsKey(m.getParentId())) {
                tree.add(m);
            } else {
                SysMenu parent = map.get(m.getParentId());
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(m);
            }
        }
        return tree;
    }
}
