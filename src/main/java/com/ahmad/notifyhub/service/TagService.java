package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.request.CreateTagRequest;
import com.ahmad.notifyhub.dto.request.UpdateTagRequest;
import com.ahmad.notifyhub.dto.response.TagResponse;
import com.ahmad.notifyhub.entity.Tag;
import com.ahmad.notifyhub.entity.User;
import com.ahmad.notifyhub.exception.ResourceNotFoundException;
import com.ahmad.notifyhub.exception.TagAlreadyExistsException;
import com.ahmad.notifyhub.mapper.TagMapper;
import com.ahmad.notifyhub.repository.TagRepository;
import com.ahmad.notifyhub.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;

    public TagService(
            TagRepository tagRepository,
            UserRepository userRepository,
            TagMapper tagMapper
    ) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.tagMapper = tagMapper;
    }

    @Transactional
    public TagResponse createTag(CreateTagRequest request, String userEmail) {
        String name = request.name().trim();

        if (tagRepository.existsByNameIgnoreCaseAndUserEmail(name, userEmail)) {
            throw new TagAlreadyExistsException("Tag name already exists");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        LocalDateTime now = LocalDateTime.now();

        Tag tag = new Tag(name, user, now, now);
        return tagMapper.toResponse(tagRepository.save(tag));
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTags(String userEmail) {
        return tagRepository.findByUserEmailOrderByNameAsc(userEmail)
                .stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    @Transactional
    public TagResponse updateTag(Long tagId, UpdateTagRequest request, String userEmail) {
        Tag tag = getOwnedTag(tagId, userEmail);
        String name = request.name().trim();

        if (!tag.getName().equalsIgnoreCase(name)
                && tagRepository.existsByNameIgnoreCaseAndUserEmail(name, userEmail)) {
            throw new TagAlreadyExistsException("Tag name already exists");
        }

        tag.rename(name, LocalDateTime.now());
        return tagMapper.toResponse(tag);
    }

    @Transactional
    public void deleteTag(Long tagId, String userEmail) {
        Tag tag = getOwnedTag(tagId, userEmail);
        LocalDateTime now = LocalDateTime.now();

        tag.getTasks().forEach(task -> task.removeTag(tag, now));
        tagRepository.delete(tag);
    }

    private Tag getOwnedTag(Long tagId, String userEmail) {
        return tagRepository.findByIdAndUserEmail(tagId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }
}
