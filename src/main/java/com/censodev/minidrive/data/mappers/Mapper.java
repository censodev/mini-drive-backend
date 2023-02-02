package com.censodev.minidrive.data.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Mapper<E, D> {
    protected final ObjectMapper mapper;
    protected final Class<E> entityType;
    protected final Class<D> dtoType;

    public Mapper(ObjectMapper mapper, Class<E> entityType, Class<D> dtoType) {
        this.mapper = mapper;
        this.entityType = entityType;
        this.dtoType = dtoType;
    }

    public E revert(D dto) {
        return mapper.convertValue(dto, entityType);
    }

    public D convert(E entity) {
        return mapper.convertValue(entity, dtoType);
    }
}
