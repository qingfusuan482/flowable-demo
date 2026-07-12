package org.example.security;

import lombok.RequiredArgsConstructor;
import org.example.model.entity.SysMenu;
import org.example.model.entity.SysUser;
import org.example.repository.SysMenuMapper;
import org.example.repository.SysUserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 查询用户菜单权限
        List<SysMenu> menus = menuMapper.selectByUserId(user.getUserId());
        List<SimpleGrantedAuthority> authorities = menus.stream()
                .map(SysMenu::getPermission)
                .filter(Objects::nonNull)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                true, true, true,
                authorities);
    }
}
