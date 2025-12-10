package za.ac.styling.service;

import java.util.List;

/**
 * Generic service interface providing CRUD operations
 * @param <T> Entity type
 * @param <ID> ID type
 */
public interface IService<T, ID> {

    /**
     * Create an entity
     */
    T create(T entity);

    /**
     * Read entity by ID
     */
    T read(ID id);

    /**
     * Update an entity
     */
    T update(T entity);

    /**
     * Get all entities
     */
    List<T> getAll();
}
