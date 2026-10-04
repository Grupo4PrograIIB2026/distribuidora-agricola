package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.BitacoraMovimiento;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la Bitacora de movimientos usando JPA/EntityManager.
 *
 * insertar() NUNCA lanza excepcion hacia afuera: si por alguna razon no
 * se puede escribir en la bitacora (ej. falta crear la tabla), la
 * operacion del usuario (guardar un cliente, registrar un pedido...)
 * no debe romperse por eso; solo se muestra un aviso.
 */
public class BitacoraDb {

    public boolean insertar(BitacoraMovimiento m) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(m);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Aviso: no se pudo guardar el movimiento en la bitacora "
                    + "(verifica que exista la tabla BitacoraMovimientos, ver sql/BitacoraMovimientos.sql): "
                    + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public List<BitacoraMovimiento> listarTodos() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<BitacoraMovimiento> q = em.createQuery(
                    "SELECT m FROM BitacoraMovimiento m ORDER BY m.idMovimiento DESC", BitacoraMovimiento.class);
            return q.getResultList();
        } catch (RuntimeException e) {
            System.out.println("Error al listar la bitacora: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<BitacoraMovimiento> listarPorUsuario(String nombreUsuario) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<BitacoraMovimiento> q = em.createQuery(
                    "SELECT m FROM BitacoraMovimiento m WHERE m.nombreUsuario = :nombreUsuario "
                    + "ORDER BY m.idMovimiento DESC", BitacoraMovimiento.class);
            q.setParameter("nombreUsuario", nombreUsuario);
            return q.getResultList();
        } catch (RuntimeException e) {
            System.out.println("Error al listar la bitacora del usuario: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}
