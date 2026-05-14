package com.piisw.tod.repository.spec;

import com.piisw.tod.model.Ad;
import com.piisw.tod.model.AdStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class AdSpecifications {

    private AdSpecifications() {
    }

    public static Specification<Ad> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("status"), AdStatus.PUBLISHED);
    }

    public static Specification<Ad> textInTitleOrDescription(String text) {
        if (text == null || text.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String like = "%" + text.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Ad> hasAnyTagName(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        List<String> normalized = tagNames.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase())
                .distinct()
                .toList();

        if (normalized.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> tagJoin = root.join("tags", JoinType.LEFT);
            var in = cb.in(cb.lower(tagJoin.get("name")));
            for (String n : normalized) {
                in.value(n);
            }
            return in;
        };
    }
}
