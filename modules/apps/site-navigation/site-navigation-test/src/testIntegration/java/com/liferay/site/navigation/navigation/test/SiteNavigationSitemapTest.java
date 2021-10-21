/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.site.navigation.navigation.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.action.SitemapAction;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.VirtualHost;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutSetService;
import com.liferay.portal.kernel.service.VirtualHostLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author David Tello
 */
@RunWith(Arquillian.class)
public class SiteNavigationSitemapTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		TreeMap<String, String> virtualHost = new TreeMap<>();

		virtualHost.put("liferaytest.com", null);

		_layoutSet = _layoutSetService.updateVirtualHosts(
			_group.getGroupId(), false, virtualHost);

		_virtualHost = _virtualHostLocalService.getVirtualHost(
			"liferaytest.com");

		_page1 = LayoutTestUtil.addLayout(_group, false);
		_page2 = LayoutTestUtil.addLayout(_group, false);
	}

	@Test
	public void testSiteNavigationPreviewSitemapFromDefaultVirtualhost()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader("host", "localhost");
		mockHttpServletRequest.setParameter(
			"groupId", String.valueOf(_group.getGroupId()));

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay(true));

		SitemapAction sitemapAction = new SitemapAction();

		sitemapAction.execute(
			null, mockHttpServletRequest, mockHttpServletResponse);

		String resultContent = mockHttpServletResponse.getContentAsString();

		StringBuilder message = new StringBuilder();

		message.append("The layout frienlyURL is: ");
		message.append(_page2.getFriendlyURL());
		message.append("The result content is: ");

		Assert.assertNotEquals(
			message.toString(), -1,
			resultContent.indexOf(_page2.getFriendlyURL()));
	}

	@Test
	public void testSiteNavigationPreviewSitemapFromVirtualhost()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader("host", "liferaytest.com");

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay(false));

		SitemapAction sitemapAction = new SitemapAction();

		sitemapAction.execute(
			null, mockHttpServletRequest, mockHttpServletResponse);

		String resultContent = mockHttpServletResponse.getContentAsString();

		StringBuilder message = new StringBuilder();

		message.append("The layout frienlyURL is: ");
		message.append(_page2.getFriendlyURL());
		message.append("The result content is: ");

		Assert.assertNotEquals(
			message.toString(), -1,
			resultContent.indexOf(_page2.getFriendlyURL()));
	}

	private ThemeDisplay _getThemeDisplay(boolean defaultGroup)
		throws Exception {

		ThemeDisplay themeDisplay = new ThemeDisplay();

		Company company = null;

		if (!defaultGroup) {
			company = _companyLocalService.getCompany(_group.getCompanyId());
			themeDisplay.setSiteGroupId(_group.getGroupId());
		}
		else {
			company = _companyLocalService.getCompany(
				TestPropsValues.getCompanyId());
			themeDisplay.setSiteGroupId(TestPropsValues.getGroupId());
		}

		themeDisplay.setCompany(company);

		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());

		themeDisplay.setUser(TestPropsValues.getUser());

		themeDisplay.setLayoutSet(_layoutSet);

		return themeDisplay;
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@DeleteAfterTestRun
	private LayoutSet _layoutSet;

	@Inject
	private LayoutSetService _layoutSetService;

	@DeleteAfterTestRun
	private Layout _page1;

	@DeleteAfterTestRun
	private Layout _page2;

	@DeleteAfterTestRun
	private VirtualHost _virtualHost;

	@Inject
	private VirtualHostLocalService _virtualHostLocalService;

}