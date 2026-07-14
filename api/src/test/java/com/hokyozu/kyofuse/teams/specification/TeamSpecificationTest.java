package com.hokyozu.kyofuse.teams.specification;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEFAULTS;

@SuppressWarnings({"rawtypes", "unchecked"})
class TeamSpecificationTest {

    @Test
    void textSpecificationsReturnConjunctionWhenValueHasNoText() {
        CriteriaMocks mocks = criteriaMocks();

        assertThat(TeamSpecification.nameContains(" ").toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);
        assertThat(TeamSpecification.hasSlug(null).toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);
        assertThat(TeamSpecification.hasRegion("").toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);

        verify(mocks.cb, org.mockito.Mockito.times(3)).conjunction();
    }

    @Test
    void nameContainsBuildsLowercaseLikePredicate() {
        CriteriaMocks mocks = criteriaMocks();
        Path namePath = mock(Path.class);
        Expression lowerName = mock(Expression.class);
        when(mocks.root.get("name")).thenReturn(namePath);
        when(mocks.cb.lower(namePath)).thenReturn(lowerName);
        when(mocks.cb.like(lowerName, "%academy%")).thenReturn(mocks.predicate);

        Predicate predicate = TeamSpecification.nameContains("  Academy  ")
                .toPredicate(mocks.root, mocks.query, mocks.cb);

        assertThat(predicate).isSameAs(mocks.predicate);
        verify(mocks.cb).like(lowerName, "%academy%");
    }

    @Test
    void slugAndRegionBuildLowercaseEqualPredicates() {
        CriteriaMocks mocks = criteriaMocks();
        Path slugPath = mock(Path.class);
        Path regionPath = mock(Path.class);
        Expression lowerSlug = mock(Expression.class);
        Expression lowerRegion = mock(Expression.class);
        when(mocks.root.get("slug")).thenReturn(slugPath);
        when(mocks.root.get("region")).thenReturn(regionPath);
        when(mocks.cb.lower(slugPath)).thenReturn(lowerSlug);
        when(mocks.cb.lower(regionPath)).thenReturn(lowerRegion);
        when(mocks.cb.equal((Expression<?>) lowerSlug, "my-team")).thenReturn(mocks.predicate);
        when(mocks.cb.equal((Expression<?>) lowerRegion, "br")).thenReturn(mocks.predicate);

        assertThat(TeamSpecification.hasSlug("  My-Team  ").toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);
        assertThat(TeamSpecification.hasRegion("  BR  ").toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);

        verify(mocks.cb).equal(lowerSlug, "my-team");
        verify(mocks.cb).equal(lowerRegion, "br");
    }

    @Test
    void statusUsesNotInactiveByDefaultAndEqualWhenProvided() {
        CriteriaMocks mocks = criteriaMocks();
        Path statusPath = mock(Path.class);
        when(mocks.root.get("status")).thenReturn(statusPath);
        when(mocks.cb.notEqual(statusPath, TeamStatus.INACTIVE)).thenReturn(mocks.predicate);
        when(mocks.cb.equal(statusPath, TeamStatus.ACTIVE)).thenReturn(mocks.predicate);

        assertThat(TeamSpecification.hasStatus(null).toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);
        assertThat(TeamSpecification.hasStatus(TeamStatus.ACTIVE).toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);

        verify(mocks.cb).notEqual(statusPath, TeamStatus.INACTIVE);
        verify(mocks.cb).equal(statusPath, TeamStatus.ACTIVE);
    }

    @Test
    void requiredRoleSpecificationUsesSubqueryWhenRolesProvided() {
        CriteriaMocks mocks = criteriaMocks();
        Subquery<UUID> subquery = mock(Subquery.class);
        Root<TeamRequiredRole> roleRoot = mock(Root.class);
        Path teamPath = mock(Path.class);
        Path teamIdPath = mock(Path.class);
        Path roleNamePath = mock(Path.class);
        Path rootIdPath = mock(Path.class, invocation ->
                invocation.getMethod().getName().equals("in")
                        ? mocks.predicate
                        : RETURNS_DEFAULTS.answer(invocation)
        );
        List<PlayerRole> roles = List.of(PlayerRole.AWPER, PlayerRole.RIFLER);
        when(mocks.query.subquery(UUID.class)).thenReturn(subquery);
        when(subquery.from(TeamRequiredRole.class)).thenReturn(roleRoot);
        when(roleRoot.get("team")).thenReturn(teamPath);
        when(teamPath.get("id")).thenReturn(teamIdPath);
        when(roleRoot.get("roleName")).thenReturn(roleNamePath);
        when(roleNamePath.in(roles)).thenReturn(mocks.predicate);
        when(subquery.select(teamIdPath)).thenReturn(subquery);
        when(subquery.where(mocks.predicate)).thenReturn(subquery);
        when(mocks.root.get("id")).thenReturn(rootIdPath);

        Predicate predicate = TeamSpecification.hasAnyRequiredRole(roles)
                .toPredicate(mocks.root, mocks.query, mocks.cb);

        assertThat(predicate).isSameAs(mocks.predicate);
        verify(subquery).select(teamIdPath);
        verify(subquery).where(mocks.predicate);
    }

    @Test
    void requiredRoleSpecificationReturnsConjunctionWhenRolesAreEmpty() {
        CriteriaMocks mocks = criteriaMocks();

        assertThat(TeamSpecification.hasAnyRequiredRole(List.of()).toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);
        assertThat(TeamSpecification.hasAnyRequiredRole(null).toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);

        verify(mocks.cb, org.mockito.Mockito.times(2)).conjunction();
    }

    @Test
    void numericSpecificationsUseRangePredicatesOrConjunctionWhenValueIsNull() {
        assertGreaterThanOrEqual(TeamSpecification.minPremierRatingAtLeast(1000), "minPremierRating", 1000);
        assertGreaterThanOrEqual(TeamSpecification.minFaceitLevelAtLeast(3), "minFaceitLevel", 3);
        assertGreaterThanOrEqual(TeamSpecification.minGcRankAtLeast(5), "minGcRank", 5);
        assertLessThanOrEqual(TeamSpecification.maxPremierRatingAtMost(20000), "maxPremierRating", 20000);
        assertLessThanOrEqual(TeamSpecification.maxFaceitLevelAtMost(8), "maxFaceitLevel", 8);
        assertLessThanOrEqual(TeamSpecification.maxGcRankAtMost(12), "maxGcRank", 12);

        CriteriaMocks nullMocks = criteriaMocks();
        assertThat(TeamSpecification.minPremierRatingAtLeast(null).toPredicate(nullMocks.root, nullMocks.query, nullMocks.cb))
                .isSameAs(nullMocks.predicate);
        verify(nullMocks.cb).conjunction();

        CriteriaMocks nullMaxMocks = criteriaMocks();
        assertThat(TeamSpecification.maxPremierRatingAtMost(null).toPredicate(nullMaxMocks.root, nullMaxMocks.query, nullMaxMocks.cb))
                .isSameAs(nullMaxMocks.predicate);
        verify(nullMaxMocks.cb).conjunction();
    }

    private void assertGreaterThanOrEqual(Specification<Team> specification, String field, Integer value) {
        CriteriaMocks mocks = criteriaMocks();
        Path path = mock(Path.class);
        when(mocks.root.get(field)).thenReturn(path);
        when(mocks.cb.greaterThanOrEqualTo(path, value)).thenReturn(mocks.predicate);

        assertThat(specification.toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);
        verify(mocks.cb).greaterThanOrEqualTo(path, value);
    }

    private void assertLessThanOrEqual(Specification<Team> specification, String field, Integer value) {
        CriteriaMocks mocks = criteriaMocks();
        Path path = mock(Path.class);
        when(mocks.root.get(field)).thenReturn(path);
        when(mocks.cb.lessThanOrEqualTo(path, value)).thenReturn(mocks.predicate);

        assertThat(specification.toPredicate(mocks.root, mocks.query, mocks.cb))
                .isSameAs(mocks.predicate);
        verify(mocks.cb).lessThanOrEqualTo(path, value);
    }

    private CriteriaMocks criteriaMocks() {
        Root<Team> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(predicate);
        return new CriteriaMocks(root, query, cb, predicate);
    }

    private record CriteriaMocks(
            Root<Team> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Predicate predicate
    ) {
    }
}
