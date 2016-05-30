void setup()
{
  Serial.begin(9600);
}
 
void loop() 
{
  if (Serial.available()) {
    byte data = Serial.read();
    Serial.write(data);
  }
}
