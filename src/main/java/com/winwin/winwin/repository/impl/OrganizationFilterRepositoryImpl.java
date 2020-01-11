/**
 * Class OrganizationFilterRepositoryImpl returns the Total Organizations based on filter criteria
 */
package com.winwin.winwin.repository.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.util.StringUtils;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.OrganizationFilterPayload;
import com.winwin.winwin.repository.OrganizationFilterRepository;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Repository
@Transactional(readOnly = true)
public class OrganizationFilterRepositoryImpl implements OrganizationFilterRepository {

	@PersistenceContext
	EntityManager entityManager;

	/**
	 * Get Organization List by OrganizationFilterPayload
	 * 
	 * @param payload
	 * @param type
	 * @param orgId
	 * @param pageNo
	 * @param pageSize
	 */
	@Override
	public List<Organization> filterOrganization(OrganizationFilterPayload payload, String type, Long orgId,
			Integer pageNo, Integer pageSize) {
		Query filterQuery = setFilterQuery(payload, type);

		try {
			@SuppressWarnings("unchecked")
			List<Organization> organizationList = filterQuery.setMaxResults(pageSize).setFirstResult(pageNo * pageSize)
					.getResultList();
			return organizationList;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}

	}

	/**
	 * Get Organization Total Count by OrganizationFilterPayload
	 * 
	 * @param payload
	 * @param type
	 * @param orgId
	 */
	@Override
	public BigInteger getFilterOrganizationCount(OrganizationFilterPayload payload, String type, Long orgId) {
		Query filterQuery = setFilterQueryForOrgCounts(payload, type);
		BigInteger count = null;
		try {
			count = (BigInteger) filterQuery.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return count;
	}

	/**
	 * @param payload
	 * @param type
	 * @return
	 */
	private Query setFilterQuery(OrganizationFilterPayload payload, String type) {
		StringBuilder query = new StringBuilder("select distinct o.* ");
		boolean spi = false;
		boolean sdg = false;
		StringBuilder sb = new StringBuilder();

		if (!StringUtils.isNullOrEmpty(payload.getAddress())) {
			query.append(", a.city, a.state ");
		} else if (!StringUtils.isNullOrEmpty(payload.getCountry()) || !StringUtils.isNullOrEmpty(payload.getState())
				|| !StringUtils.isNullOrEmpty(payload.getCity()) || !StringUtils.isNullOrEmpty(payload.getCounty())) {
			query.append(", a.city, a.state ");
		} else if ((!StringUtils.isNullOrEmpty(payload.getSortBy())) && payload.getSortBy().matches("(?i)city|state")) {
			query.append(", a.city, a.state ");
		}

		query.append("from organization o inner join address a on a.id = o.address_id");

		sb.append(" where  o.is_active = true and o.type = :type ");

		if ((payload.getRevenueMin() != null && payload.getRevenueMax() != null)
				|| (payload.getRevenueMin() != null && payload.getRevenueMax() == null)
				|| (payload.getRevenueMin() == null && payload.getRevenueMax() != null))
			sb.append(" and (coalesce(o.revenue,0) BETWEEN :minRevenue and :maxRevenue )");

		if ((payload.getAssetsMin() != null && payload.getAssetsMax() != null)
				|| (payload.getAssetsMin() != null && payload.getAssetsMax() == null)
				|| (payload.getAssetsMin() == null && payload.getAssetsMax() != null))
			sb.append(" and (coalesce(o.assets,0) BETWEEN :minAssets and  :maxAssets ) ");

		if (payload.getSectorLevel() != null && payload.getSectorLevel().size() != 0) {
			sb.append(" and (o.sector_level IN :sectorLevel) ");
		}

		if (payload.getSectors() != null && payload.getSectors().size() != 0) {
			sb.append(" and (o.sector IN :sectors) ");
		}

		if (payload.getTagStatus() != null && payload.getTagStatus().size() != 0)
			sb.append(" and (o.tag_status IN :tagStatus) ");

		if (!StringUtils.isNullOrEmpty(payload.getPriority()))
			sb.append(" and o.priority IS NOT DISTINCT FROM :priority ");

		if (payload.getCreatedBy() != null && payload.getCreatedBy().size() != 0)
			sb.append(" and o.created_by_email IN :createdByEmail ");

		if (payload.getEditedBy() != null && payload.getEditedBy().size() != 0)
			sb.append(" and o.updated_by_email IN :editedByEmail ");

		if (payload.getNteeCode() != null && payload.getNteeCode() != 0)
			sb.append(" and o.ntee_code IS NOT DISTINCT FROM :nteeCode ");

		if (payload.getNaicsCode() != null && payload.getNaicsCode() != 0)
			sb.append(" and o.naics_code IS NOT DISTINCT FROM :naicsCode ");

		if (payload.getFrameworkTag() != null && payload.getFrameworkTag().equalsIgnoreCase("SPI")) {
			query.append(" join org_spi_mapping osm on o.id=osm.organization_id join ")
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
			query.append(" join org_sdg_mapping osm on o.id=osm.organization_id join "
					+ " sdg_data osd on osm.sdg_id = osd.id ");

			sb.append(" AND osm.is_checked = true ");

			if (!StringUtils.isNullOrEmpty(payload.getShortNameCode()))
				sb.append(" and osd.short_name_code IS NOT DISTINCT FROM :shortNameCode ");

			if (payload.getGoalCode() != 0)
				sb.append(" and osd.goal_code IS NOT DISTINCT FROM :goalCode");
			sdg = true;
		}

		if (!StringUtils.isNullOrEmpty(payload.getNameSearch())) {
			sb.append("  AND o.name ILIKE :name");
		}

		if (!StringUtils.isNullOrEmpty(payload.getAddress())) {
			sb.append(
					"  AND (a.country ILIKE :country or a.state ILIKE :state or a.county ILIKE :county or a.city ILIKE :city)");
		} else {
			if (!StringUtils.isNullOrEmpty(payload.getCountry())) {
				sb.append("  AND (a.country ILIKE '%" + payload.getCountry() + "%' )");
			}
			if (!StringUtils.isNullOrEmpty(payload.getState())) {
				sb.append("  AND (a.state ILIKE '%" + payload.getState() + "%' )");
			}
			if (!StringUtils.isNullOrEmpty(payload.getCity())) {
				sb.append("  AND (a.city ILIKE '%" + payload.getCity() + "%' )");
			}
			if (!StringUtils.isNullOrEmpty(payload.getCounty())) {
				sb.append("  AND (a.county ILIKE '%" + payload.getCounty() + "%' )");
			}
		}

		// keep the empty values always last
		if (!StringUtils.isNullOrEmpty(payload.getSortBy()))
			sb.append(" order by " + payload.getSortBy() + " "
					+ (!StringUtils.isNullOrEmpty(payload.getSortOrder()) ? payload.getSortOrder() + " NULLS LAST"
							: "ASC NULLS LAST"));
		else
			sb.append(" order by o.name ASC NULLS LAST");

		query.append(sb);
		Query filterQuery = entityManager.createNativeQuery(query.toString(), Organization.class);
		filterQuery.setParameter("type", type);

		if ((payload.getRevenueMin() != null && payload.getRevenueMax() != null)) {
			filterQuery.setParameter("minRevenue", payload.getRevenueMin());
			filterQuery.setParameter("maxRevenue", payload.getRevenueMax());
		} else if (payload.getRevenueMin() == null && payload.getRevenueMax() != null) {
			filterQuery.setParameter("minRevenue", Double.MIN_VALUE);
			filterQuery.setParameter("maxRevenue", payload.getRevenueMax());
		} else if (payload.getRevenueMin() != null && payload.getRevenueMax() == null) {
			filterQuery.setParameter("minRevenue", payload.getRevenueMin());
			filterQuery.setParameter("maxRevenue", Double.MAX_VALUE);
		}

		if ((payload.getAssetsMin() != null && payload.getAssetsMax() != null)) {
			filterQuery.setParameter("minAssets", payload.getAssetsMin());
			filterQuery.setParameter("maxAssets", payload.getAssetsMax());
		} else if (payload.getAssetsMin() == null && payload.getAssetsMax() != null) {
			filterQuery.setParameter("minAssets", Double.MIN_VALUE);
			filterQuery.setParameter("maxAssets", payload.getAssetsMax());
		} else if (payload.getAssetsMin() != null && payload.getAssetsMax() == null) {
			filterQuery.setParameter("minAssets", payload.getAssetsMin());
			filterQuery.setParameter("maxAssets", Double.MAX_VALUE);
		}

		if (payload.getSectorLevel() != null && payload.getSectorLevel().size() != 0)
			filterQuery.setParameter("sectorLevel", payload.getSectorLevel());

		if (payload.getSectors() != null && payload.getSectors().size() != 0)
			filterQuery.setParameter("sectors", payload.getSectors());

		if (payload.getTagStatus() != null && payload.getTagStatus().size() != 0)
			filterQuery.setParameter("tagStatus", payload.getTagStatus());

		if (!StringUtils.isNullOrEmpty(payload.getPriority()))
			filterQuery.setParameter("priority", payload.getPriority());

		if (payload.getNteeCode() != null && payload.getNteeCode() != 0)
			filterQuery.setParameter("nteeCode", payload.getNteeCode());

		if (payload.getNaicsCode() != null && payload.getNaicsCode() != 0)
			filterQuery.setParameter("naicsCode", payload.getNaicsCode());

		if (payload.getCreatedBy() != null && payload.getCreatedBy().size() != 0)
			filterQuery.setParameter("createdByEmail", payload.getCreatedBy());

		if (payload.getEditedBy() != null && payload.getEditedBy().size() != 0)
			filterQuery.setParameter("editedByEmail", payload.getEditedBy());

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

		if (!StringUtils.isNullOrEmpty(payload.getNameSearch()))
			filterQuery.setParameter("name", "%" + payload.getNameSearch() + "%");

		if (!StringUtils.isNullOrEmpty(payload.getAddress()) && StringUtils.isNullOrEmpty(payload.getCountry())
				&& StringUtils.isNullOrEmpty(payload.getState()) && StringUtils.isNullOrEmpty(payload.getCity())
				&& StringUtils.isNullOrEmpty(payload.getCounty())) {
			filterQuery.setParameter("country", "%" + payload.getAddress() + "%");
			filterQuery.setParameter("state", "%" + payload.getAddress() + "%");
			filterQuery.setParameter("city", "%" + payload.getAddress() + "%");
			filterQuery.setParameter("county", "%" + payload.getAddress() + "%");

		}

		return filterQuery;
	}

	/**
	 * @param payload
	 * @param type
	 * @return
	 */
	private Query setFilterQueryForOrgCounts(OrganizationFilterPayload payload, String type) {
		StringBuilder query = new StringBuilder("select count(distinct o.id) from organization o ");
		boolean spi = false;
		boolean sdg = false;
		StringBuilder sb = new StringBuilder();

		if (!StringUtils.isNullOrEmpty(payload.getAddress())) {
			query.append("inner join address a on a.id = o.address_id");
		} else if (!StringUtils.isNullOrEmpty(payload.getCountry()) || !StringUtils.isNullOrEmpty(payload.getState())
				|| !StringUtils.isNullOrEmpty(payload.getCity()) || !StringUtils.isNullOrEmpty(payload.getCounty())) {
			query.append("inner join address a on a.id = o.address_id");
		}

		sb.append(" where  o.is_active = true and o.type = :type ");

		if ((payload.getRevenueMin() != null && payload.getRevenueMax() != null)
				|| (payload.getRevenueMin() != null && payload.getRevenueMax() == null)
				|| (payload.getRevenueMin() == null && payload.getRevenueMax() != null))
			sb.append(" and (coalesce(o.revenue,0) BETWEEN :minRevenue and :maxRevenue )");

		if ((payload.getAssetsMin() != null && payload.getAssetsMax() != null)
				|| (payload.getAssetsMin() != null && payload.getAssetsMax() == null)
				|| (payload.getAssetsMin() == null && payload.getAssetsMax() != null))
			sb.append(" and (coalesce(o.assets,0) BETWEEN :minAssets and  :maxAssets ) ");

		if (payload.getSectorLevel() != null && payload.getSectorLevel().size() != 0) {
			sb.append(" and (o.sector_level IN :sectorLevel) ");
		}

		if (payload.getSectors() != null && payload.getSectors().size() != 0) {
			sb.append(" and (o.sector IN :sectors) ");
		}

		if (payload.getTagStatus() != null && payload.getTagStatus().size() != 0)
			sb.append(" and (o.tag_status IN :tagStatus) ");

		if (!StringUtils.isNullOrEmpty(payload.getPriority()))
			sb.append(" and o.priority IS NOT DISTINCT FROM :priority ");

		if (payload.getCreatedBy() != null && payload.getCreatedBy().size() != 0)
			sb.append(" and o.created_by_email IN :createdByEmail ");

		if (payload.getEditedBy() != null && payload.getEditedBy().size() != 0)
			sb.append(" and o.updated_by_email IN :editedByEmail ");

		if (payload.getNteeCode() != null && payload.getNteeCode() != 0)
			sb.append(" and o.ntee_code IS NOT DISTINCT FROM :nteeCode ");

		if (payload.getNaicsCode() != null && payload.getNaicsCode() != 0)
			sb.append(" and o.naics_code IS NOT DISTINCT FROM :naicsCode ");

		if (payload.getFrameworkTag() != null && payload.getFrameworkTag().equalsIgnoreCase("SPI")) {
			query.append(" join org_spi_mapping osm on o.id=osm.organization_id join ")
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
			query.append(" join org_sdg_mapping osm on o.id=osm.organization_id join "
					+ " sdg_data osd on osm.sdg_id = osd.id ");

			sb.append(" AND osm.is_checked = true ");

			if (!StringUtils.isNullOrEmpty(payload.getShortNameCode()))
				sb.append(" and osd.short_name_code IS NOT DISTINCT FROM :shortNameCode ");

			if (payload.getGoalCode() != 0)
				sb.append(" and osd.goal_code IS NOT DISTINCT FROM :goalCode");
			sdg = true;
		}

		if (!StringUtils.isNullOrEmpty(payload.getNameSearch())) {
			sb.append("  AND o.name ILIKE :name");
		}

		if (!StringUtils.isNullOrEmpty(payload.getAddress())) {
			sb.append(
					"  AND (a.country ILIKE :country or a.state ILIKE :state or a.county ILIKE :county or a.city ILIKE :city)");
		} else {
			if (!StringUtils.isNullOrEmpty(payload.getCountry())) {
				sb.append("  AND (a.country ILIKE '%" + payload.getCountry() + "%' )");
			}
			if (!StringUtils.isNullOrEmpty(payload.getState())) {
				sb.append("  AND (a.state ILIKE '%" + payload.getState() + "%' )");
			}
			if (!StringUtils.isNullOrEmpty(payload.getCity())) {
				sb.append("  AND (a.city ILIKE '%" + payload.getCity() + "%' )");
			}
			if (!StringUtils.isNullOrEmpty(payload.getCounty())) {
				sb.append("  AND (a.county ILIKE '%" + payload.getCounty() + "%' )");
			}
		}

		query.append(sb);
		Query filterQuery = entityManager.createNativeQuery(query.toString());

		filterQuery.setParameter("type", type);

		if ((payload.getRevenueMin() != null && payload.getRevenueMax() != null)) {
			filterQuery.setParameter("minRevenue", payload.getRevenueMin());
			filterQuery.setParameter("maxRevenue", payload.getRevenueMax());
		} else if (payload.getRevenueMin() == null && payload.getRevenueMax() != null) {
			filterQuery.setParameter("minRevenue", Double.MIN_VALUE);
			filterQuery.setParameter("maxRevenue", payload.getRevenueMax());
		} else if (payload.getRevenueMin() != null && payload.getRevenueMax() == null) {
			filterQuery.setParameter("minRevenue", payload.getRevenueMin());
			filterQuery.setParameter("maxRevenue", Double.MAX_VALUE);
		}

		if ((payload.getAssetsMin() != null && payload.getAssetsMax() != null)) {
			filterQuery.setParameter("minAssets", payload.getAssetsMin());
			filterQuery.setParameter("maxAssets", payload.getAssetsMax());
		} else if (payload.getAssetsMin() == null && payload.getAssetsMax() != null) {
			filterQuery.setParameter("minAssets", Double.MIN_VALUE);
			filterQuery.setParameter("maxAssets", payload.getAssetsMax());
		} else if (payload.getAssetsMin() != null && payload.getAssetsMax() == null) {
			filterQuery.setParameter("minAssets", payload.getAssetsMin());
			filterQuery.setParameter("maxAssets", Double.MAX_VALUE);
		}

		if (payload.getSectorLevel() != null && payload.getSectorLevel().size() != 0)
			filterQuery.setParameter("sectorLevel", payload.getSectorLevel());

		if (payload.getSectors() != null && payload.getSectors().size() != 0)
			filterQuery.setParameter("sectors", payload.getSectors());

		if (payload.getTagStatus() != null && payload.getTagStatus().size() != 0)
			filterQuery.setParameter("tagStatus", payload.getTagStatus());

		if (!StringUtils.isNullOrEmpty(payload.getPriority()))
			filterQuery.setParameter("priority", payload.getPriority());

		if (payload.getNteeCode() != null && payload.getNteeCode() != 0)
			filterQuery.setParameter("nteeCode", payload.getNteeCode());

		if (payload.getNaicsCode() != null && payload.getNaicsCode() != 0)
			filterQuery.setParameter("naicsCode", payload.getNaicsCode());

		if (payload.getCreatedBy() != null && payload.getCreatedBy().size() != 0)
			filterQuery.setParameter("createdByEmail", payload.getCreatedBy());

		if (payload.getEditedBy() != null && payload.getEditedBy().size() != 0)
			filterQuery.setParameter("editedByEmail", payload.getEditedBy());

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

		if (!StringUtils.isNullOrEmpty(payload.getNameSearch()))
			filterQuery.setParameter("name", "%" + payload.getNameSearch() + "%");

		if (!StringUtils.isNullOrEmpty(payload.getAddress()) && StringUtils.isNullOrEmpty(payload.getCountry())
				&& StringUtils.isNullOrEmpty(payload.getState()) && StringUtils.isNullOrEmpty(payload.getCity())
				&& StringUtils.isNullOrEmpty(payload.getCounty())) {
			filterQuery.setParameter("country", "%" + payload.getAddress() + "%");
			filterQuery.setParameter("state", "%" + payload.getAddress() + "%");
			filterQuery.setParameter("city", "%" + payload.getAddress() + "%");
			filterQuery.setParameter("county", "%" + payload.getAddress() + "%");

		}

		return filterQuery;
	}
}
