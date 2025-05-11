package org.retrade.common.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.cglib.core.internal.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@NoRepositoryBean
public interface BaseJpaRepository <T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    default Specification<T> queryAnySpecification(QueryWrapper queryWrapper) {
        return queryAnySpecification(queryWrapper.search());
    }
    default Specification<T> queryAnySpecification(Map<String, QueryFieldWrapper> queryWrapper) {
        return (root, query, criteriaBuilder) -> {
            if (queryWrapper == null || queryWrapper.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            Predicate[] defaultPredicates = createDefaultPredicate(criteriaBuilder, root, queryWrapper);
            return criteriaBuilder.and(defaultPredicates);
        };
    }
    default Predicate[] createDefaultPredicate(
            CriteriaBuilder criteriaBuilder,
            Root<?> root,
            Map<String, QueryFieldWrapper> queryWrapper
    ) {
        return queryWrapper.entrySet().stream().map(entry -> {
            String field = entry.getKey();
            QueryFieldWrapper wrapper = entry.getValue();
            Object value = wrapper.getValue();

            return switch (wrapper.getOperator()) {
                case EQ -> buildComparisonPredicate(criteriaBuilder, root, field, value, ComparisonOperator.EQ);
                case NE -> criteriaBuilder.notEqual(root.get(field), value);
                case LIKE -> criteriaBuilder.like(root.get(field), "%" + value.toString() + "%");

                case GT -> buildComparisonPredicate(criteriaBuilder, root, field, value, ComparisonOperator.GT);
                case GTE -> buildComparisonPredicate(criteriaBuilder, root, field, value, ComparisonOperator.GTE);
                case LT -> buildComparisonPredicate(criteriaBuilder, root, field, value, ComparisonOperator.LT);
                case LTE -> buildComparisonPredicate(criteriaBuilder, root, field, value, ComparisonOperator.LTE);

                case IN -> {
                    if (value instanceof Collection<?> collection) {
                        if ("id".equals(field)) {
                            yield root.get(field).in(collection.stream().map(val -> Long.parseLong(val.toString())).toList());
                        } else {
                            yield root.get(field).in(collection.stream().map(Object::toString).toList());
                        }
                    }
                    yield criteriaBuilder.conjunction();
                }

                case BETWEEN -> {
                    if (value instanceof List<?> range && range.size() == 2) {
                        if ("id".equals(field)) {
                            yield criteriaBuilder.between(
                                    root.get(field).as(Long.class),
                                    Long.parseLong(range.get(0).toString()),
                                    Long.parseLong(range.get(1).toString())
                            );
                        } else {
                            yield criteriaBuilder.between(
                                    root.get(field),
                                    range.get(0).toString(),
                                    range.get(1).toString()
                            );
                        }
                    }
                    yield criteriaBuilder.conjunction();
                }

                default -> criteriaBuilder.conjunction();
            };
        }).toArray(Predicate[]::new);
    }

    private Predicate buildComparisonPredicate(
            CriteriaBuilder cb,
            Root<?> root,
            String field,
            Object value,
            ComparisonOperator op
    ) {
        Path<?> path = root.get(field);
        Class<?> fieldType = path.getJavaType();

        try {
            if (fieldType.equals(Boolean.class)) {
                return op.build(cb, root.get(field), Boolean.parseBoolean(value.toString()));
            } else if (fieldType.equals(Integer.class)) {
                return op.build(cb, root.get(field), Integer.parseInt(value.toString()));
            } else if (fieldType.equals(Long.class)) {
                return op.build(cb, root.get(field), Long.parseLong(value.toString()));
            } else if (fieldType.equals(Float.class)) {
                return op.build(cb, root.get(field), Float.parseFloat(value.toString()));
            } else if (fieldType.equals(Double.class)) {
                return op.build(cb, root.get(field), Double.parseDouble(value.toString()));
            } else if (fieldType.equals(BigDecimal.class)) {
                return op.build(cb, root.get(field), new BigDecimal(value.toString()));
            } else if (fieldType.equals(LocalDate.class)) {
                return op.build(cb, root.get(field), LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else if (fieldType.equals(LocalDateTime.class)) {
                String dateStr = value.toString();
                DateTimeFormatter formatter;
                if (dateStr.contains(".")) {
                    int len = dateStr.substring(dateStr.indexOf('.') + 1).length();
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss" + (len == 3 ? ".SSS" : len == 6 ? ".SSSSSS" : ""));
                } else {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                }
                return op.build(cb, root.get(field), LocalDateTime.parse(dateStr, formatter));
            } else if (fieldType.equals(Instant.class)) {
                return op.build(cb, root.get(field), Instant.parse(value.toString()));
            } else {
                return op.build(cb, root.get(field), value.toString());
            }
        } catch (Exception e) {
            return cb.conjunction();
        }
    }


    enum ComparisonOperator {
        EQ {
            public <T extends Comparable<? super T>> Predicate build(CriteriaBuilder cb, Path<T> path, T val) {
                return cb.equal(path, val);
            }
        },
        GT {
            public <T extends Comparable<? super T>> Predicate build(CriteriaBuilder cb, Path<T> path, T val) {
                return cb.greaterThan(path, val);
            }
        },
        GTE {
            public <T extends Comparable<? super T>> Predicate build(CriteriaBuilder cb, Path<T> path, T val) {
                return cb.greaterThanOrEqualTo(path, val);
            }
        },
        LT {
            public <T extends Comparable<? super T>> Predicate build(CriteriaBuilder cb, Path<T> path, T val) {
                return cb.lessThan(path, val);
            }
        },
        LTE {
            public <T extends Comparable<? super T>> Predicate build(CriteriaBuilder cb, Path<T> path, T val) {
                return cb.lessThanOrEqualTo(path, val);
            }
        };

        public abstract <T extends Comparable<? super T>> Predicate build(CriteriaBuilder cb, Path<T> path, T val);
    }
    default Predicate[] createDefaultPredicate(CriteriaBuilder criteriaBuilder, Root<?> root, QueryWrapper queryWrapper) {
        return createDefaultPredicate(criteriaBuilder, root, queryWrapper.search());
    }
    default Page<T> queryAny(QueryWrapper queryWrapper, Pageable pageable) {
        Specification<T> spec = queryAnySpecification(queryWrapper);
        return findAll(spec, pageable);
    }

    default Page<T> queryAny(Map<String, QueryFieldWrapper> queryWrapper, Pageable pageable) {
        Specification<T> spec = queryAnySpecification(queryWrapper);
        return findAll(spec, pageable);
    }

    default Page<T> query(Map<String, QueryFieldWrapper> param, Pageable pageable, Function<Map<String, QueryFieldWrapper>, Specification<T>> query) {
        return findAll(query.apply(param), pageable);
    }
    default Page<T> query(QueryWrapper queryWrapper, Function<Map<String, QueryFieldWrapper>, Specification<T>> query) {
        return query(queryWrapper.search(), queryWrapper.pagination(), query);
    }
    default Page<T> query(Specification<T> query, Pageable pageable) {
        return findAll(query, pageable);
    }
    default <D extends List<?>> PaginationWrapper<D> query(Map<String, QueryFieldWrapper> param, Pageable pageable, Function<Map<String, QueryFieldWrapper>, Specification<T>> query, Function<Page<T>, PaginationWrapper<D>> mapper) {
        var entityResult = findAll(query.apply(param), pageable);
        return mapper.apply(entityResult);
    }
    default <D extends List<?>> PaginationWrapper<D> query(QueryWrapper queryWrapper, Function<Map<String, QueryFieldWrapper>, Specification<T>> query, Function<Page<T>, PaginationWrapper<D>> mapper) {
        return query(queryWrapper.search(), queryWrapper.pagination(), query, mapper);
    }
}
