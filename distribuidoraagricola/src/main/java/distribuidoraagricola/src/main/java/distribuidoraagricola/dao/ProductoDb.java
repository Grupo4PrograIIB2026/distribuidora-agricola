package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.ProductoAgricola;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Productos agricolas usando JPA/EntityManager.
 *
 * buscarPorId(EntityManager, int) es una sobrecarga que participa en
 * la transaccion del llamador (CompraService/PedidoService la usan
 * para traer el producto YA GESTIONADO por ese EntityManager y poder
 * cambiar su existencia; JPA detecta el cambio solo -"dirty checking"-
 * y lo guarda al hacer commit, sin necesidad de un UPDATE manual).
 */
public class ProductoDb {

    public boolean insertar(ProductoAgricola p) {
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
            System.out.println("Error al crear producto: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean actualizar(ProductoAgricola p) {
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
            System.out.println("Error al actualizar producto: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(int idProducto) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            ProductoAgricola p = em.find(ProductoAgricola.class, idProducto);
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
            System.out.println("Error al eliminar producto (verifica que no tenga compras/pedidos asociados): " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean existeId(int idProducto) {
        return buscarPorId(idProducto) != null;
    }

    public ProductoAgricola buscarPorId(int idProducto) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            return em.find(ProductoAgricola.class, idProducto);
        } catch (RuntimeException e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    /** Sobrecarga que participa en la transaccion del llamador (ver nota de la clase). */
    public ProductoAgricola buscarPorId(EntityManager em, int idProducto) {
        return em.find(ProductoAgricola.class, idProducto);
    }

    public List<ProductoAgricola> listarTodos() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<ProductoAgricola> q = em.createQuery(
                    "SELECT p FROM ProductoAgricola p ORDER BY p.idProducto", ProductoAgricola.class);
            return q.getResultList();
        } catch (RuntimeException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public int consultarExistencia(int idProducto) {
        ProductoAgricola p = buscarPorId(idProducto);
        return p == null ? -1 : p.getExistencia();
    }
}
