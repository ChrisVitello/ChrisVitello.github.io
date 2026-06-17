# Title: VitelloWeb
# Authors: Miraj Acharya, Christopher Vitello
# December 2025
from os import remove

from flask import Flask, render_template, request, redirect, url_for, session
import flask_login
from flask_sqlalchemy import *
from flask_login import UserMixin, login_user, logout_user, current_user


app = Flask(__name__)


#DATABASE:

    # Creates a database file if not already made, and stores in users.db using sqlite
app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///users.db"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False # Turns off unneeded feature
db = SQLAlchemy(app) # db is an object. SQLAlchemy is configured and connected to the flask app
    #User class,
class User(db.Model):
    # Sets up id, username, password
    #id is set as primary key
    id = db.Column(db.Integer, primary_key=True)
    #username must be unique
    username = db.Column(db.String(80), unique=True, nullable=False)
    #password cannot be null
    password = db.Column(db.String(80), nullable=False)

with app.app_context():
    #Flask knows which app its from
    #Creates the database
    db.create_all()


admin = False
currentUser = "Guest"


# Just for the testing
adminInfo = ["VitelloWeb", "CV0809"]



#WEB APP ITSELF:
# Home page
@app.route("/")
def home():
    return render_template("home.html", role = currentUser)


# About page
@app.route('/about')
def about():
    return render_template("about.html", role = currentUser)


# URL for login AND
# had GET --> user opens page
# and POST --> user submits form
@app.route('/login', methods=['GET', 'POST'])
def login():
    global admin
    global currentUser
    # IF user clicked the login button,
    # take the username and password they submitted
    if request.method == 'POST':
        username = request.form["username"]
        password = request.form["password"]
        if username == adminInfo[0] and password == adminInfo[1]:
            admin = True
            currentUser = username + " | Admin"
            #return "Hello Admin!" # To ensure the code ran correct
            return render_template("home.html", role = currentUser)
        elif User.query.filter_by(username=username).first():
            user = User.query.filter_by(username=username).first()
            currentUser = username
            if user and user.password == password:
                #return "You're in!" # Ensures code runs correct
                return render_template("home.html", role = currentUser)
        error = "Invalid username or password"
        return render_template("login.html", error=error)
    # UNFINISHED!!!
    return render_template("login.html", role = currentUser)


#Create Signup page
@app.route('/signup', methods=["GET", "POST"])
def signup():
    error = None # initial
    if request.method == "POST":
        # Requests form for username and password
        username = request.form["username"]
        password = request.form["password"]

        #If username is already created, return error
        #Below line checks the database and returns a user object if found
        #if not, returns a None
        if User.query.filter_by(username=username).first() is not None:
            error = "Username already exists"


        else:
            #Creates new user object
            user = User(username = username, password = password)
            #Adds to database
            db.session.add(user)
            #Commits it fully to database
            db.session.commit()
            return render_template("login.html", role = currentUser)
    return render_template("signup.html", error = error)

@app.route('/recipe')
def rec1():
    return render_template("recipe.html",
                           name = "Pizza",
                           role = currentUser,
                           bio = "Pizza",
                           description = "This dish is cheese and a variety of other toppings cooked in a tomato sauce on top of a fluffy, bready crust.",
                           topping = "Cheese",
                           add = "Pepperoni", add1 = "Sausage", add2 = "Mushrooms", add3 = "Spinach",
                           mod = "Change to white Pie", mod1 = "Change to margherita pie", mod2 = "Buffallo Sauce", mod3 = "Hot Sauce",
                           link = "https://hips.hearstapps.com/hmg-prod/images/classic-cheese-pizza-recipe-2-64429a0cb408b.jpg?crop=0.8888888888888888xw:1xh;center,top&resize=1200:*")

if admin:
    @app.route('/admin')
    def admin():
        return render_template("admin.html", role = currentUser)


@app.route('/cart')
def cart():
    toppings = request.args.get('toppings')
    modification = request.args.get('modifications')
    extra = request.args.get('Extra Information')
    return render_template("cart.html", role = currentUser, toppings = toppings)






if __name__ == '__main__':
    app.run(debug=True)

