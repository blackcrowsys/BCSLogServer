/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


/**
 * @author ramindursingh
 *
 */
public class JdbcLogsDao implements LogsDao {
    
    private static SessionFactory sessionFactory;
    
    @SuppressWarnings("unused")
    private JdbcLogsDao() {}
    
    public JdbcLogsDao(SessionFactory sessionFactory) {
        JdbcLogsDao.sessionFactory = sessionFactory;
    }

    /* (non-Javadoc)
     * @see com.blackcrowsys.bcslog.server.dao.LogsDao#save(java.lang.String, java.lang.String)
     */
    @Override
    public void save(String data, String table) {
        String sql = String.format("INSERT INTO %s (LOG) VALUES (:data)", table);
        Session session = sessionFactory.openSession();
        Transaction transaction;
        try {
            transaction = session.beginTransaction();
            Query query = session.createSQLQuery(sql);
            query.setParameter("data", data);
            query.executeUpdate();
            transaction.commit();
        }catch(HibernateException e) {
            e.printStackTrace();
        }finally {
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see com.blackcrowsys.bcslog.server.dao.LogsDao#save(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void save(String data, String table, String application) {
        String sql = String.format("INSERT INTO %s (APPLICATION, LOG) VALUES (:application, :data)", table);
        Session session = sessionFactory.openSession();
        Transaction transaction;
        try {
            transaction = session.beginTransaction();
            Query query = session.createSQLQuery(sql);
            query.setParameter("data", data);
            query.setParameter("application", application);
            query.executeUpdate();
            transaction.commit();
        }catch(HibernateException e) {
            e.printStackTrace();
        }finally {
            session.close();
        }
    }

}
