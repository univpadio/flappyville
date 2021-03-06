package flappyville;

import flappyville.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "evenementsendpoint", namespace = @ApiNamespace(ownerDomain = "flappyville", ownerName = "flappyville", packagePath = "services"))
public class EvenementsEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listEvenements")
	public CollectionResponse<Evenements> listEvenements(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Evenements> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Evenements.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Evenements>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Evenements obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Evenements> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getEvenements")
	public Evenements getEvenements(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Evenements evenements = null;
		try {
			evenements = mgr.getObjectById(Evenements.class, id);
		} finally {
			mgr.close();
		}
		return evenements;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param evenements the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertEvenements")
	public Evenements insertEvenements(Evenements evenements) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsEvenements(evenements)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(evenements);
		} finally {
			mgr.close();
		}
		return evenements;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param evenements the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateEvenements")
	public Evenements updateEvenements(Evenements evenements) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsEvenements(evenements)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(evenements);
		} finally {
			mgr.close();
		}
		return evenements;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeEvenements")
	public void removeEvenements(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Evenements evenements = mgr.getObjectById(Evenements.class, id);
			mgr.deletePersistent(evenements);
		} finally {
			mgr.close();
		}
	}

	private boolean containsEvenements(Evenements evenements) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Evenements.class, evenements.getId());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
