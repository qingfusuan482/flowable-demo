package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.model.entity.SysPost;
import org.example.repository.SysPostMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysPostService {

    private final SysPostMapper postMapper;

    public Page<SysPost> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysPost::getPostName, keyword);
        }
        wrapper.orderByAsc(SysPost::getSort);
        return postMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<SysPost> listAll() {
        return postMapper.selectList(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getStatus, 1)
                .orderByAsc(SysPost::getSort));
    }

    public SysPost getById(Long id) {
        return postMapper.selectById(id);
    }

    public void save(SysPost post) {
        postMapper.insert(post);
    }

    public void update(SysPost post) {
        postMapper.updateById(post);
    }

    public void delete(Long id) {
        postMapper.deleteById(id);
    }
}
