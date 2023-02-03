package com.censodev.minidrive.data.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Mapper<E, D> {
    protected final ObjectMapper objectMapper;
    protected final Class<E> entityType;
    protected final Class<D> dtoType;

    protected Mapper(ObjectMapper objectMapper, Class<E> entityType, Class<D> dtoType) {
        this.objectMapper = objectMapper;
        this.entityType = entityType;
        this.dtoType = dtoType;
    }

    public E revert(D dto) {
        return objectMapper.convertValue(dto, entityType);
    }

    public D convert(E entity) {
        return objectMapper.convertValue(entity, dtoType);
    }
}
