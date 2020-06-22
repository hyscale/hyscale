/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.controller.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;

/**
 * Helper class to provide troubleshoot related utilities
 *
 */
public class TroubleshootUtil {

    public static String getTroubleshootMessage(List<DiagnosisReport> diagnosisReports) {
        if (diagnosisReports == null || diagnosisReports.isEmpty()) {
            return null;
        }
        StringBuilder message = new StringBuilder();

        diagnosisReports.stream().forEach(each -> {
            if (each == null) {
                return;
            }
            String reason = each.getReason();
            if (StringUtils.isNotBlank(reason)) {
                message.append(reason);
                message.append(ToolConstants.DOT);
                message.append(ToolConstants.SPACE);
            }
            message.append(each.getRecommendedFix());
            message.append(ToolConstants.NEW_LINE);
        });
        return message.toString().trim();
    }

}
