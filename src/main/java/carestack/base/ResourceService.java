package carestack.base;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

/**
 * Defines a standard contract for resource services that support CRUD (Create, Read, Update, Delete) and search operations.
 * Using a common interface ensures API consistency across the SDK.
 *
 * @param <C> The DTO type for creating a resource.
 * @param <U> The DTO type for updating a resource.
 */
public interface ResourceService<C, U> {

    /**
     * Retrieves a paginated list of resources.
     * @param params Optional parameters: Integer (pageSize) and/or String (nextPage).
     * @return A Mono containing the paginated list of resources.
     */
    Mono<Object> findAll(Object... params);

    /**
     * Retrieves a single resource by its unique identifier.
     * @param id The unique ID of the resource.
     * @return A Mono containing the resource details.
     */
    Mono<Object> findById(String id);

    /**
     * Checks if a resource with the given ID exists.
     * @param id The unique ID of the resource.
     * @return A Mono emitting true if the resource exists, false otherwise.
     */
    Mono<Boolean> exists(String id);

    /**
     * Creates a new resource.
     * @param createDto The DTO containing the data for the new resource.
     * @return A Mono containing the API response after creation.
     */
    Mono<Object> create(@Valid C createDto);

    /**
     * Updates an existing resource.
     * @param updateDto The DTO containing the updated data.
     * @return A Mono containing the API response after the update.
     */
    Mono<Object> update(@Valid U updateDto);

    /**
     * Searches for resources based on a set of filters.
     * @param params Optional parameters: SearchFiltersDTO, Integer (pageSize), and/or String (nextPage).
     * @return A Mono containing the search results.
     */
    Mono<Object> findByFilters(Object... params);

    /**
     * Deletes a resource by its unique identifier.
     * @param id The unique ID of the resource to delete.
     * @return A Mono containing the API response after deletion.
     */
    Mono<Object> delete(String id);
}