package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.common.BusinessException;
import org.example.model.dto.LoginRequest;
import org.example.model.entity.*;
import org.example.model.vo.*;
import org.example.repository.*;
import org.example.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginVO login(LoginRequest request) {
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtTokenProvider.createToken(user.getUsername());
        UserInfoVO userInfo = buildUserInfo(user);
        return LoginVO.builder().token(token).userInfo(userInfo).build();
    }

    public UserInfoVO getUserInfo(String username) {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserInfo(user);
    }

    private UserInfoVO buildUserInfo(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setDeptId(user.getDeptId());
        vo.setDeptName(user.getDeptName());
        vo.setPostId(user.getPostId());
        vo.setStatus(user.getStatus());

        // 查询用户菜单
        List<SysMenu> menuList = menuMapper.selectByUserId(user.getUserId());
        List<MenuVO> menuTree = buildMenuTree(menuList);
        vo.setMenus(menuTree);

        // 收集权限标识
        Set<String> permissions = menuList.stream()
                .map(SysMenu::getPermission)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        vo.setPermissions(new ArrayList<>(permissions));

        return vo;
    }

    private List<MenuVO> buildMenuTree(List<SysMenu> menuList) {
        Map<Long, MenuVO> voMap = new HashMap<>();
        List<MenuVO> tree = new ArrayList<>();

        for (SysMenu menu : menuList) {
            MenuVO vo = toMenuVO(menu);
            voMap.put(menu.getMenuId(), vo);
        }

        for (SysMenu menu : menuList) {
            MenuVO vo = voMap.get(menu.getMenuId());
            if (menu.getParentId() == null || !voMap.containsKey(menu.getParentId())) {
                tree.add(vo);
            } else {
                MenuVO parent = voMap.get(menu.getParentId());
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(vo);
            }
        }
        return tree;
    }

    private MenuVO toMenuVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setMenuId(menu.getMenuId());
        vo.setParentId(menu.getParentId());
        vo.setMenuName(menu.getMenuName());
        vo.setMenuType(menu.getMenuType());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setPermission(menu.getPermission());
        vo.setSort(menu.getSort());
        vo.setVisible(menu.getVisible());
        return vo;
    }
}
