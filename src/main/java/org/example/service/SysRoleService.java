package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.model.entity.SysRole;
import org.example.model.entity.SysRoleMenu;
import org.example.model.entity.SysUserRole;
import org.example.repository.SysRoleMapper;
import org.example.repository.SysRoleMenuMapper;
import org.example.repository.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public Page<SysRole> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysRole::getRoleName, keyword);
        }
        wrapper.orderByAsc(SysRole::getSort);
        return roleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSort));
    }

    public SysRole getById(Long id) {
        return roleMapper.selectById(id);
    }

    public void save(SysRole role) {
        roleMapper.insert(role);
    }

    public void update(SysRole role) {
        roleMapper.updateById(role);
    }

    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    /** 分配角色菜单 */
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 删除旧关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        // 插入新关联
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }

    /** 获取角色拥有的菜单ID列表 */
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }
}
