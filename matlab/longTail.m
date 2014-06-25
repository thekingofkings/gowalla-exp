% plot the long-tail distribution of #checkins per user.

a = importdata('../data/userCount.txt');
ncks = a(:,2);

ru = [1500, 5000];
cks = [500, 235]; 

figure;
hold on;
plot(ncks, 'color', 'blue', 'linewidth', 3);
plot(ru, cks, 'dr', 'markersize', 9, 'linewidth', 3);
text( ru(1)+800, cks(1)+100, '(1500, 500)', 'fontsize', 20);
text( ru(2)+800, cks(2)+100, '(5000, 235)', 'fontsize', 20);
box on;
grid on;
xlabel('Rank of user', 'fontsize', 20);
ylabel('#check-ins', 'fontsize', 20);
set(gca, 'fontsize', 20, 'linewidth', 3, 'xticklabel', {0, 20000, 40000, 60000, 80000, 100000, 120000});
% axis([0,110000, 0, 2500]);
legend({'Ranked number of check-ins'}, 'location', 'northeast', ...
    'fontsize', 20);

print('GWcheckinsDist.eps', '-dpsc');
system('epstopdf GWcheckinsDist.eps');