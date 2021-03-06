/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.soap.deployers;

import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.soap.core.databinding.RubyTypeSpace;
import org.torquebox.soap.metadata.SOAPServiceMetaData;

public class RubyTypeSpaceDeployer extends AbstractDeployer {

	private static final String PREFIX = "jboss.ruby.databinding.";

	public RubyTypeSpaceDeployer() {
	    addInput(SOAPServiceMetaData.class);
		setStage(DeploymentStages.POST_CLASSLOADER);
		addOutput(BeanMetaData.class);
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		log.debug("deploying for: " + unit);
		
		Set<? extends SOAPServiceMetaData> allMetaData = unit.getAllMetaData(SOAPServiceMetaData.class );
		
	    if ( allMetaData.isEmpty() ) {
	        return;
	    }

		BeanMetaData busMetaData = unit.getAttachment(BeanMetaData.class + "$cxf.bus", BeanMetaData.class);

		
		for (SOAPServiceMetaData endpointMetaData : allMetaData ) {
			String beanName = getBeanName(unit, endpointMetaData.getName());
			BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, RubyTypeSpace.class.getName());

			builder.addPropertyMetaData("rubyPath", "jboss/databinding/" + endpointMetaData.getName() );
			builder.addPropertyMetaData("wsdlLocation", endpointMetaData.getWsdlLocation());
			builder.addPropertyMetaData("classLoader", unit.getClassLoader() );
			
			ValueMetaData busInjection = builder.createInject(busMetaData.getName());
			builder.addPropertyMetaData("bus", busInjection);

			BeanMetaData beanMetaData = builder.getBeanMetaData();
			unit.addAttachment(BeanMetaData.class.getName() + "$databinding." + endpointMetaData.getName(), beanMetaData, BeanMetaData.class);
		}
	}

	public static String getBeanName(DeploymentUnit unit, String serviceName) {
		return PREFIX + unit.getSimpleName() + "." + serviceName;
	}


}
