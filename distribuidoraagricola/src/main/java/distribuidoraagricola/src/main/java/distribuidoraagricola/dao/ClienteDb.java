package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.Cliente;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Clientes usando JPA/EntityManager. Cada metodo
 * abre y cierra su propio EntityManager (patron simple, adecuado
 * para esta aplicacion de consola de un solo usuario); ningun otro
 * DAO necesita compartir transaccion con este.
 */
public class ClienteDb {

    public boolean insertar(Cliente c) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al crear cliente: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean actualizar(Cliente c) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(int idCliente) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            Cliente c = em.find(Cliente.class, idCliente);
            if (c == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(c);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al eliminar cliente (verifica que no tenga pedidos asociados): " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean existeId(int idCliente) {
        return buscarPorId(idCliente) != null;
    }

    public Cliente buscarPorId(int idCliente) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            return em.find(Cliente.class, idCliente);
        } catch (RuntimeException e) {
            System.out.println("Error al buscar cliente: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<Cliente> listarTodos() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<Cliente> q = em.createQuery(
                    "SELECT c FROM Cliente c ORDER BY c.idCliente", Cliente.class);
            return q.getResultList();
        } catch (RuntimeException e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}
