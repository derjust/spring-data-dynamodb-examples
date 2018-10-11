/**
 * Copyright © 2018 spring-data-dynamodb-example (https://github.com/derjust/spring-data-dynamodb-examples)
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
package com.github.derjust.spring_data_dynamodb_examples.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.github.derjust.spring_data_dynamodb_examples.rest.Application;

@Component
public class MySQLShutdownApplicationListener implements ApplicationListener<ContextClosedEvent> {
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	@Override
	public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
		log.info("AbandonedConnectionCleanupThread - Shutdown initiated...");
		com.mysql.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
		log.info("AbandonedConnectionCleanupThread - Completed");
	}
}
