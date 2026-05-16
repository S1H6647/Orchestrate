package com.project.orchestrate.modules.comment_module.repository;

import com.project.orchestrate.modules.comment_module.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

//    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user WHERE pm.project.id = :projectId")

    //    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId")
    List<Comment> findAllByTaskId(UUID taskId);
}
