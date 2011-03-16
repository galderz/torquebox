# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

module VFS
  class Dir
    attr_reader :path
    attr_reader :pos
    alias_method :tell, :pos

    def initialize(path)
      @path         = path
      begin
        @virtual_file = org.jboss.vfs.VFS.child( path )
      rescue Java::JavaLang::NullPointerException
        raise Errno::ENOENT.new
      end
      @pos          = 0
      @closed       = false
    end

    def close
      @closed = true
    end

    def each
      @virtual_file.children.each do |child|
        yield child.name
      end
    end

    def rewind
      @pos = 0
    end

    def read
      children = @virtual_file.children
      return nil unless ( @pos < children.size )
      child = children[@pos]
      @pos += 1
      child.name
    end

    def seek(i)
      @pos = i
      self
    end

    def pos=(i)
      @pos = i
    end

    def entries
      [ '.', '..' ] + @virtual_file.children.map(&:name)
    end

    def exists?
      @virtual_file.exists?
    end

  end

end

