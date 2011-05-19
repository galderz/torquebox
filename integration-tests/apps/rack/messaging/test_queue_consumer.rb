require 'torquebox-messaging'

class TestQueueConsumer < TorqueBox::Messaging::MessageProcessor
  include TorqueBox::Injectors

  def on_message(body)
    puts "on_message: #{body}"
    inject('/queues/results').publish( "#{self.class.name}=#{body}" )
  end

end
