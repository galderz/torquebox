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

package org.torquebox.common.pool;

import org.jboss.logging.Logger;
import org.torquebox.common.spi.InstanceFactory;
import org.torquebox.common.spi.Pool;

/**
 * A pool implementation that shares a single instance to all consumers.
 * 
 * <p>
 * The pool may be primed with either an instance directly or by providing a
 * factory capable of creating the instance. In the case that a
 * {@link InstanceFactory} is used, exactly one instance will be created.
 * </p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @param <T>
 *            The poolable resource.
 */
public class SharedPool<T> implements Pool<T> {
    
    protected Logger log = Logger.getLogger( getClass() );
    
    /** Name of the pool. */
    private String name = "anonymous-pool";

    /** The shared instance. */
    private T instance;

    /** Optional factory to create the initial instance. */
    private InstanceFactory<T> factory;

    private boolean deferred = true;

    /**
     * Construct.
     */
    public SharedPool() {
    }

    /**
     * Construct with an instance factory.
     * 
     * @param factory
     *            The factory to create the initial instance.
     */
    public SharedPool(InstanceFactory<T> factory) {
        this.factory = factory;
    }

    /**
     * Construct with an instance.
     * 
     * @param instance
     *            The initial instance.
     */
    public SharedPool(T instance) {
        this.instance = instance;
    }

    /**
     * Set the pool name.
     * 
     * @param name
     *            The pool name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieve the pool name.
     * 
     * @return The pool name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the shared instance.
     * 
     * @param instance
     *            The initial instance.
     */
    public void setInstance(T instance) {
        this.instance = instance;
    }

    /**
     * Retrieve the shared instance.
     * 
     * @return The shared instance.
     */
    public T getInstance() {
        return this.instance;
    }

    /**
     * Set the instance factory to create the initial instance.
     * 
     * @param factory
     *            The instance factory.
     */
    public void setInstanceFactory(InstanceFactory<T> factory) {
        this.factory = factory;
    }

    /**
     * Retrieve the instance factory to create the initial instance.
     * 
     * @return The instance factory;
     */
    public InstanceFactory<T> getInstanceFactory() {
        return this.factory;
    }

    public synchronized void startPool() throws Exception {
        startPool( false );
    }

    public synchronized void startPool(boolean ignored) throws Exception {
        if (this.instance == null) {
            this.instance = factory.createInstance( getName() );
        }
    }

    public boolean isDeferred() {
        return this.deferred;
    }

    /**
     * Create the pool.
     * 
     * @throws Exception
     *             if an error occurs starting the pool.
     */
    public void create() throws Exception {
        if (this.instance != null) {
            return;
        }

        if (this.factory == null) {
            throw new IllegalArgumentException( "Neither an instance nor an instance-factory provided." );
        }
        if (!this.deferred) {
            startPool();
        } else if (this.name.equals( "web" )) {
            // Start the web pool via a thread. This is a hack, 
            // but is only for 1.1. All this goes away in 2.0
            log.info( "Starting " + this.name + " runtime pool asynchronously" );
            final SharedPool pool = this;
            Thread initThread = new Thread() {
                    public void run() {
                        try {
                            pool.startPool( false );
                        } catch(Exception ex) {
                            log.error( "Failed to start pool", ex );
                        }
                    }
                };
            initThread.start();
        } else {
            log.info( "Deferring start for " + this.name + " runtime pool." );
        }
    }

    /**
     * Destroy the pool.
     */
    public void destroy() {
        if (this.factory != null && this.instance != null) {
            this.factory.destroyInstance( this.instance );
        }
        this.instance = null;
        this.factory = null;
    }

    @Override
    public T borrowInstance() throws Exception {
        return borrowInstance( 0 );
    }

    @Override
    public void releaseInstance(T instance) {
        // nothing
    }

    @Override
    public T borrowInstance(long timeout) throws Exception {
        if (this.instance == null) {
            startPool();
        }
        return this.instance;
    }

}
