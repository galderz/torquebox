module TorqueBox
  module Authentication
    def self.[](name)
      return nil unless torquebox_context
      puts "Looking up authentication context #{torquebox_context}-authentication-#{name}"
      Authenticator.new(::TorqueBox::Kernel.lookup(torquebox_context + "-authentication-" + name))
    end

    def self.default
      self['default']
    end

    private
    def self.torquebox_context
      puts "ERROR: TorqueBox application context not available" unless ENV['TORQUEBOX_APP_NAME']
      ENV['TORQUEBOX_APP_NAME']
    end
  end

  class Authenticator
    def initialize(auth_bean)
      @auth_bean = auth_bean
      puts "Initializing TorqueBox Authenticator with #{auth_bean.inspect}"
    end

    def authenticate(user, pass, &block)
      if @auth_bean.nil?
        puts "ERROR: No authentication delegate found. Authentication not enabled." 
        return false 
      end

      authenticated = @auth_bean.authenticate(user, pass)
      block.call if authenticated && block
      authenticated
    end
  end
end
