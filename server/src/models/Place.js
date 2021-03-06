module.exports = (sequalize, DataTypes) => {
  const Place = sequalize.define('Place', {
    email: {
      type: DataTypes.STRING,
      unique: true
    },
    type: DataTypes.STRING,
    name: DataTypes.STRING,
    address: DataTypes.STRING,
    telephone: DataTypes.STRING,
    description: DataTypes.STRING,
    image: DataTypes.STRING,
    avgRating: {
      type: DataTypes.INTEGER,
      defaultValue: 0
    }
  })
  return Place
}
