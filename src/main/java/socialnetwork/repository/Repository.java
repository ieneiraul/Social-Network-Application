package socialnetwork.repository;

import socialnetwork.domain.Entity;
import socialnetwork.domain.Eveniment;
import socialnetwork.domain.Message;
import socialnetwork.domain.validators.ValidationException;

/**
 * CRUD operations repository interface
 * @param <ID> - type E must have an attribute of type ID
 * @param <E> -  type of entities saved in repository
 */

public interface Repository<ID, E extends Entity<ID>> {

    /**
     *
     * @param id -the id of the entity to be returned
     *           id must not be null
     * @return the entity with the specified id
     *          or null - if there is no entity with the given id
     * @throws IllegalArgumentException
     *                  if id is null.
     */
    E findOne(ID id);

    /**
     *
     * @return all entities
     */
    Iterable<E> findAll();

    Iterable<E> findFromOffset(Long idUtilizatorCurent ,Long offset, Long limit);

    /**
     *
     * @param entity
     *         entity must be not null
     * @return null- if the given entity is saved
     *         otherwise returns the entity (id already exists)
     * @throws ValidationException
     *            if the entity is not valid
     * @throws IllegalArgumentException
     *             if the given entity is null.     *
     */
    E save(E entity);

    public void addUtilizatorInscris(long id_e, long id_u);
    public Iterable<Eveniment> findUsersEventsFromOffset(Long idUtilizatorCurent, Long offset, Long limit);
    public Iterable<Message> find2UsersMessagesFromOffset(Long idUtilizatorCurent, Long idPrieten, Long offset, Long limit);
    public int nrElemOf2Users(long id1, long id2);
    public int nrElemOfAnUser(long id);
    public Eveniment deleteUserOfAnEvent(Long iduser, Long ideveniment);

    /**
     *  removes the entity with the specified id
     * @param id
     *      id must be not null
     * @return the removed entity or null if there is no entity with the given id
     * @throws IllegalArgumentException
     *                   if the given id is null.
     */
    E delete(ID id);

    /**
     *
     * @param entity
     *          entity must not be null
     * @return null - if the entity is updated,
     *                otherwise  returns the entity  - (e.g id does not exist).
     * @throws IllegalArgumentException
     *             if the given entity is null.
     * @throws ValidationException
     *             if the entity is not valid.
     */
    E update(E entity);


    /**numarul de elemente din map
     */
    int nrElem();


}

