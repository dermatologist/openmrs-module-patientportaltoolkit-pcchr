package org.openmrs.module.patientportaltoolkit.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientportaltoolkit.PatientPortalRelation;
import org.openmrs.module.patientportaltoolkit.api.db.PatientPortalRelationDAO;

import java.util.Date;
import java.util.List;

/**
 * Created by Maurya.
 */
public class HibernatePatientPortalRelationDAO implements PatientPortalRelationDAO {

    protected final Log log = LogFactory.getLog(getClass());

    private SessionFactory sessionFactory;

    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public PatientPortalRelation getPatientPortalRelation(String uuid) {
        final Criteria crit = this.sessionFactory.getCurrentSession().createCriteria(PatientPortalRelation.class);
        crit.add(Restrictions.eq("uuid", uuid));
        return (PatientPortalRelation) crit.uniqueResult();
    }

    @Override
    public PatientPortalRelation savePatientPortalRelation(PatientPortalRelation patientPortalRelation) {
        sessionFactory.getCurrentSession().saveOrUpdate(patientPortalRelation);
        return patientPortalRelation;
    }

    @Override
    public void deletePatientPortalRelation(PatientPortalRelation patientPortalRelation) {
        sessionFactory.getCurrentSession().delete(patientPortalRelation);
    }

    @Override
    public List<PatientPortalRelation> getAllPatientPortalRelation() {
        final Criteria crit = this.sessionFactory.getCurrentSession().createCriteria(PatientPortalRelation.class);
        crit.addOrder(Order.asc("patient"));
        this.log.debug("HibernatePatientPortalRelationDAO:getAllPatientPortalRelation->" + " | token count=" + crit.list().size());
        return crit.list();
    }

    @Override
    public List<PatientPortalRelation> getPatientPortalRelationByPatient(Patient patient) {
        final Criteria crit = this.sessionFactory.getCurrentSession().createCriteria(PatientPortalRelation.class);
        crit.add(Restrictions.eq("patient", patient));
        crit.addOrder(Order.desc("dateCreated"));
        final List<PatientPortalRelation> list = crit.list();
        this.log.debug("HibernatePatientPortalSharingTokenDAO:getSharingTokenByPatient->" + patient + " | token count=" + list.size());
        if (list.size() >= 1) {
            return list;
        } else {
            return null;
        }
    }

    @Override
    public List<PatientPortalRelation> getPatientPortalRelationByPerson(Person person) {
        if (person instanceof Patient) {
            return getPatientPortalRelationByPatient((Patient) person);
        }

        final Criteria crit = this.sessionFactory.getCurrentSession().createCriteria(PatientPortalRelation.class);
        crit.add(Restrictions.eq("relatedPerson", person));
        crit.addOrder(Order.desc("dateCreated"));
        final List<PatientPortalRelation> list = crit.list();
        this.log.debug("HibernatePatientPortalSharingTokenDAO:getSharingTokenByPerson->" + person + " | token count=" + list.size());
        if (list.size() >= 1) {
            return list;
        } else {
            return null;
        }
    }

    @Override
    public PatientPortalRelation getPatientPortalRelation(Patient requestedPatient, Person requestedPerson, User requestingUser) {
        Patient pat = requestedPatient;

        if ((pat == null) && (requestedPerson != null)) {
            pat = Context.getPatientService().getPatient(requestedPerson.getPersonId());
            this.log.debug("getSharingToken for person|patient->" + requestedPerson + "|" + pat);
        }

        Person per = requestingUser.getPerson();

        Criteria crit = this.sessionFactory.getCurrentSession().createCriteria(PatientPortalRelation.class);
        crit.add(Restrictions.eq("relatedPerson", per));
        crit.add(Restrictions.eq("patient", pat));
        crit.addOrder(Order.desc("dateCreated"));

        List<PatientPortalRelation> list = crit.list();

        this.log.debug("HibernatePatientPortalSharingTokenDAO:getSharingToken->" + requestedPatient + "|" + requestedPerson + "|"
                + requestingUser + "|token count=" + list.size());

        if (list.size() >= 1) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void updatePatientPortalRelation(User user, Person person, String uuid) {
        final PatientPortalRelation patientPortalRelation = getPatientPortalRelation(uuid);
        if (patientPortalRelation != null) {
        final Date date = new Date();

        if (patientPortalRelation.getExpireDate().after(date)) {
            if (patientPortalRelation.getRelatedPerson() == null) {
                patientPortalRelation.setRelatedPerson(person);
                patientPortalRelation.setChangedBy(user);
                patientPortalRelation.setDateChanged(date);
                patientPortalRelation.setActivateDate(date);
                savePatientPortalRelation(patientPortalRelation);
                this.log.debug("Sharing token updated: " + patientPortalRelation.getId());
            } else {
                this.log.debug("Sharing token is igored because it was activated before by: " + patientPortalRelation.getChangedBy()
                        + " at " + patientPortalRelation.getActivateDate());
            }
        } else {
            this.log.debug("Sharing token is ignored because it expired at " + patientPortalRelation.getExpireDate());
        }
    } else {
        this.log.debug("Sharing token is ignored because it is invalid: " + uuid);
    }

    }
}
