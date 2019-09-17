/**
 * Class OrganizationFilterRepositoryImpl returns the Total Programs based on filter criteria
 */
package com.winwin.winwin.repository.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.Program;
import com.winwin.winwin.payload.ProgramFilterPayloadData;
import com.winwin.winwin.repository.ProgramFilterRepository;

/**
 * @author ArvindKhatik
 * @version 1.0
 */

@Repository
@Transactional(readOnly = true)
public class ProgramFilterRepositoryImpl implements ProgramFilterRepository {

	@PersistenceContext
	EntityManager entityManager;

	/**
	 * Get Program List by ProgramFilter Payload
	 * 
	 * @param payload
	 */
	@Override
	public List<Program> filterProgram(ProgramFilterPayloadData payload, String type, Long orgId) {
		// TODO Auto-generated method stub
		StringBuilder query = new StringBuilder("select distinct p.* from program p ");
		StringBuilder sb = new StringBuilder();
		sb.append(" where  p.is_active = true ");

		// if (type.equals(OrganizationConstants.PROGRAM))
		sb.append(" and p.org_id = :orgId ");

		if (payload.getEditedBy() != null && payload.getEditedBy().size() != 0)
			sb.append(" and p.updated_by IN :editedBy ");

		query.append(sb);
		Query filterQuery = entityManager.createNativeQuery(query.toString(), Program.class);

		// if (type.equals(OrganizationConstants.PROGRAM))
		filterQuery.setParameter("orgId", orgId);

		if (payload.getEditedBy() != null && payload.getEditedBy().size() != 0)
			filterQuery.setParameter("editedBy", payload.getEditedBy());

		List<Program> programList = filterQuery.getResultList();

		return programList;
	}
}
