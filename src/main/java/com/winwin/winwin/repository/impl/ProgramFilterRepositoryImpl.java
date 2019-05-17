package com.winwin.winwin.repository.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.util.StringUtils;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.repository.ProgramFilterRepository;

@Repository
@Transactional(readOnly = true)
public class ProgramFilterRepositoryImpl implements ProgramFilterRepository {

	@PersistenceContext
	EntityManager entityManager;

	@Override
	public List<Program> filterProgram(OrganizationFilterPayload payload, String type, Long orgId) {
		// TODO Auto-generated method stub
		StringBuilder query = new StringBuilder("select distinct o.* from program o ");
		boolean spi = false;
		boolean sdg = false;
		StringBuilder sb = new StringBuilder();
		sb.append(" where  o.is_Active = true ");

		// if (type.equals(OrganizationConstants.PROGRAM))
		sb.append(" and o.org_id = :orgId ");

		sb.append(" and (coalesce(o.revenue,0) BETWEEN :minRevenue and :maxRevenue )");
		sb.append(" and (coalesce(o.assets,0) BETWEEN :minAssets and  :maxAssets ) ");

		if (payload.getSectorLevel() != null && payload.getSectorLevel().size() != 0) {
//			String inQuery = "( ";
//			int i = 0;
//			for (; i < payload.getSectorLevel().size() - 1; i++)
//				inQuery = inQuery + ":sectorLevel" + i + " , ";
//			inQuery = inQuery + " :sectorLevel" + i + " ) ";
//			sb.append(" and ( o.sector_level IN ").append(inQuery).append(" ) ");
			sb.append(" and (o.sector_level IN :sectorLevel) ");
		}

		if (payload.getTagStatus() != null && payload.getTagStatus().size() != 0)
			sb.append(" and (o.tag_status IN :tagStatus) ");

		if (!StringUtils.isNullOrEmpty(payload.getPriority()))
			sb.append(" and o.priority IS NOT DISTINCT FROM :priority ");

		if (payload.getEditedBy() != null && payload.getEditedBy().size() != 0)
			sb.append(" and o.updated_by IN :editedBy ");

		if (payload.getNteeCode() != null && payload.getNteeCode() != 0)
			sb.append(" and o.ntee_code IS NOT DISTINCT FROM :nteeCode ");

		if (payload.getNaicsCode() != null && payload.getNaicsCode() != 0)
			sb.append(" and o.naics_code IS NOT DISTINCT FROM :naicsCode ");

		if (payload.getFrameworkTag() != null && payload.getFrameworkTag().equalsIgnoreCase("SPI")) {

			query.append("join program_spi_mapping osm on o.id=osm.program_id join ")
					.append(" spi_data osd on osm.spi_id = osd.id ");

			sb.append(" AND osm.is_checked = true ");
			if (!StringUtils.isNullOrEmpty(payload.getIndicatorId()))
				sb.append(" and osd.indicator_id IS NOT DISTINCT FROM :indicatorId ");

			if (!StringUtils.isNullOrEmpty(payload.getComponentId()))
				sb.append(" and osd.component_id IS NOT DISTINCT FROM :componentId ");

			if (payload.getDimensionId() != 0)
				sb.append(" and osd.dimension_id IS NOT DISTINCT FROM :dimensionId ");
			spi = true;
		} else if (payload.getFrameworkTag() != null && payload.getFrameworkTag().equalsIgnoreCase("SDG")) {
			query.append("join program_sdg_mapping osm on o.id=osm.program_id join "
					+ " sdg_data osd on osm.sdg_id = osd.id ");

			sb.append(" AND osm.is_checked = true ");

			if (!StringUtils.isNullOrEmpty(payload.getShortNameCode()))
				sb.append(" and osd.short_name_code IS NOT DISTINCT FROM :shortNameCode ");

			if (payload.getGoalCode() != 0)
				sb.append(" and osd.goal_code IS NOT DISTINCT FROM :goalCode");
			sdg = true;
		}

		query.append(sb);
		Query filterQuery = entityManager.createNativeQuery(query.toString(), Program.class);

		// filterQuery.setParameter("type", type);

		// if (type.equals(OrganizationConstants.PROGRAM))
		filterQuery.setParameter("orgId", orgId);

		filterQuery.setParameter("minRevenue", payload.getRevenueMin());
		filterQuery.setParameter("maxRevenue", payload.getRevenueMax());
		filterQuery.setParameter("minAssets", payload.getAssetsMin());
		filterQuery.setParameter("maxAssets", payload.getAssetsMax());

		if (payload.getSectorLevel() != null && payload.getSectorLevel().size() != 0)
			filterQuery.setParameter("sectorLevel", payload.getSectorLevel());

		if (payload.getTagStatus() != null && payload.getTagStatus().size() != 0)
			filterQuery.setParameter("tagStatus", payload.getTagStatus());

		if (!StringUtils.isNullOrEmpty(payload.getPriority()))
			filterQuery.setParameter("priority", payload.getPriority());

		if (payload.getNteeCode() != null && payload.getNteeCode() != 0)
			filterQuery.setParameter("nteeCode", payload.getNteeCode());

		if (payload.getNaicsCode() != null && payload.getNaicsCode() != 0)
			filterQuery.setParameter("naicsCode", payload.getNaicsCode());

		if (payload.getEditedBy() != null && payload.getEditedBy().size() != 0)
			filterQuery.setParameter("editedBy", payload.getEditedBy());

		if (!StringUtils.isNullOrEmpty(payload.getIndicatorId()) && spi)
			filterQuery.setParameter("indicatorId", payload.getIndicatorId());

		if (!StringUtils.isNullOrEmpty(payload.getComponentId()) && spi)
			filterQuery.setParameter("componentId", payload.getComponentId());

		if (payload.getDimensionId() != 0 && spi)
			filterQuery.setParameter("dimensionId", payload.getDimensionId());

		if (!StringUtils.isNullOrEmpty(payload.getShortNameCode()) && sdg)
			filterQuery.setParameter("shortNameCode", payload.getShortNameCode());

		if (payload.getGoalCode() != 0 && sdg)
			filterQuery.setParameter("goalCode", payload.getGoalCode());

		List<Program> programList = filterQuery.getResultList();

		return programList;
	}
}
