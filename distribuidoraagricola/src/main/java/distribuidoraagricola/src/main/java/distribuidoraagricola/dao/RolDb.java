package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.Rol;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Roles usando JPA/EntityManager.
 */
public class RolDb {

    public List<Rol> listarTodos() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<Rol> q = em.createQuery("SELECT r FROM Rol r ORDER BY r.idRol", Rol.class);
            return q.getResultList();
        } catch (RuntimeException e) {
            System.out.println("Error al listar roles: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Rol buscarPorNombre(String nombreRol) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<Rol> q = em.createQuery(
                    "SELECT r FROM Rol r WHERE r.nombreRol = :nombreRol", Rol.class);
            q.setParameter("nombreRol", nombreRol);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (RuntimeException e) {
            System.out.println("Error al buscar rol: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }
}
