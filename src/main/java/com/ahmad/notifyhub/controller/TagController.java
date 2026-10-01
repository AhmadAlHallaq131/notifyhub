package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.request.CreateTagRequest;
import com.ahmad.notifyhub.dto.request.UpdateTagRequest;
import com.ahmad.notifyhub.dto.response.TagResponse;
import com.ahmad.notifyhub.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody CreateTagRequest request,
            Authentication authentication
    ) {
        TagResponse response = tagService.createTag(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TagResponse> getTags(Authentication authentication) {
        return tagService.getTags(authentication.getName());
    }

    @PutMapping("/{tagId}")
    public TagResponse updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody UpdateTagRequest request,
            Authentication authentication
    ) {
        return tagService.updateTag(tagId, request, authentication.getName());
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long tagId,
            Authentication authentication
    ) {
        tagService.deleteTag(tagId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
