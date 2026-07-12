package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.BusinessException;
import org.example.model.entity.*;
import org.example.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;
    private final PasswordEncoder passwordEncoder;

    public IPage<SysUser> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        // 填充部门名称和岗位名称
        for (SysUser user : page.getRecords()) {
            fillExtInfo(user);
        }
        return page;
    }

    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) fillExtInfo(user);
        return user;
    }

    @Transactional
    public void save(SysUser user) {
        // 检查用户名唯一
        SysUser exist = userMapper.selectByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Transactional
    public void update(SysUser user) {
        // 密码不为空时才更新密码
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码字段
        }
        userMapper.updateById(user);
    }

    @Transactional
    public void delete(Long id) {
        userMapper.deleteById(id);
        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    private void fillExtInfo(SysUser user) {
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) user.setDeptName(dept.getDeptName());
        }
        if (user.getPostId() != null) {
            SysPost post = postMapper.selectById(user.getPostId());
            if (post != null) user.setPostName(post.getPostName());
        }
    }
}
