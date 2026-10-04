package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.Proveedor;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Proveedores usando JPA/EntityManager.
 */
public class ProveedorDb {

    public boolean insertar(Proveedor p) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al crear proveedor: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean actualizar(Proveedor p) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(p);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al actualizar proveedor: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(int idProveedor) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            Proveedor p = em.find(Proveedor.class, idProveedor);
            if (p == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(p);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al eliminar proveedor (verifica que no tenga compras asociadas): " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean existeId(int idProveedor) {
        return buscarPorId(idProveedor) != null;
    }

    public Proveedor buscarPorId(int idProveedor) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            return em.find(Proveedor.class, idProveedor);
        } catch (RuntimeException e) {
            System.out.println("Error al buscar proveedor: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<Proveedor> listarTodos() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<Proveedor> q = em.createQuery(
                    "SELECT p FROM Proveedor p ORDER BY p.idProveedor", Proveedor.class);
            return q.getResultList();
        } catch (RuntimeException e) {
            System.out.println("Error al listar proveedores: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}
