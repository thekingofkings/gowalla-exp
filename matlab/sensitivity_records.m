
files = ls('../data_sensit_recs/distance-d30-u5000-us*.txt');
    

% sort the files by the number of users
numUser = zeros(size(files,1), 1);
for i = 1:size(files,1)
    tmp = textscan(files(i,:), 'distance-d30-u5000-us%f.txt');
    numUser(i) = tmp{1};
end
[~, ind] = sort(numUser, 'ascend');
files = files(ind, :);



figure();
hold on;
box on;
grid on;
    
colors = [ [0, 0, 0.8]; [0.3, 0.6, 0.9]; ...
    [0, 100, 0] / 255; [0, 0.75, 0]; ...
    [255, 140, 0]/ 255; [255, 215, 0] / 255;  ...
    [153,50,204]/255; [216,191,216] /255; ...
    [143,188,143]/255; [0,128,128]/255];
color_ind = 1;
g = zeros(1, size(files,1));
auc = zeros(size(files, 1)+1, 4);
for ind = 1:size(files,1)
    
    data = importdata(['../data_sensit_recs/', files(ind,:)]);
    data = data(data(:,6)>0,:);
    [~, i] = sort(data(:,6));
    data = data(i, :);  

    size(data)

    pbg_locen = data(:,3);
    locen = data(:,4);
    pbg = data(:,5);
    freq = data(:,6);
    pbg_locen_td = data(:,7);
    td = data(:,8);
    friflag = data(:,9);


    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;
%     prec_rec( prod_colcEnt_cm, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
%     prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:');
%     prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--');
    % My own precision-recall plot function    

    l = zeros(1,3);
    [pre, rec, l(1)] = precisionRecallPlot( freq, friflag, 'linestyle', '-', 'color', colors(color_ind,:) );
    auc(ind+1,1) = trapz(rec, pre);
%     precisionRecallPlot( pbg, friflag, 'r--' );
%     precisionRecallPlot( locen, friflag, 'linestyle', '--', 'color', [0, 0.75, 0] );
%     precisionRecallPlot( td, friflag, 'linestyle', '--', 'color', [255, 215, 0] / 255 );
    
    [pre, rec, l(2)] = precisionRecallPlot( pbg, friflag, 'linestyle', ':', 'color', colors(color_ind,:) );
    auc(ind+1,2) = trapz(rec, pre);
    [pre, rec, l(3)] = precisionRecallPlot( pbg_locen_td, friflag, 'linestyle', '--', 'color', colors(color_ind,:) );
    auc(ind+1,3) = trapz(rec, pre);
    [pre, rec, l(4)] = precisionRecallPlot( locen, friflag, 'linestyle', '--', 'color', colors(color_ind,:) );
    auc(ind+1,4) = trapz(rec, pre);
    color_ind = color_ind + 1;

    g(ind) = hggroup;
    set(l(1:3), 'Parent', g(ind));
end

%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend([g], {'Freq/PBG/PGT 20%', 'Freq/PBG/PGT 50%', ...
        'Freq/PBG/PGT 80%', 'Freq/PBG/PGT 100%'}, 'location', 'northeast');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
%     print('sensitivity-recs.eps', '-dpsc');
%     system('epstopdf sensitivity-recs.eps');
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);


frequency = auc(:,1);
pbg = auc(:,2);
pbg_locen_td = auc(:,3);
locenAUC = auc(:,4);
x = 0:51.2:512.75;
x = round(x);


figure;
hold on;
grid on;
box on;
plot(x, frequency, '-', 'color', [200, 0, 0] / 255, 'linewidth', 3);
plot(x, pbg, '--', 'color', [0.3, 0.6, 0.9], 'linewidth', 3);
plot(x, pbg_locen_td, '-.', 'color', [0, 100, 0] / 255, 'linewidth', 3);
% plot(x, locenAUC, '-.', 'color', [100, 100, 0] / 255, 'linewidth', 3);
set(gca, 'linewidth', 2, 'fontsize', 18, 'xtick', x(3:2:11), 'xticklabel',...
    {'20%', '40%', '60%', '80%', '100%'});
labX = x(3:2:11) - 20;
labY = 0.03 * ones(size(labX));
text(labX, labY, num2cell(x(3:2:11)), 'fontsize', 20);
xlabel('Average #check-ins (sample rate)', 'fontsize', 20);
ylabel('AUC', 'fontsize', 20);
legend({'Frequency', 'Personal', 'Per+Glo+Tem'}, 'location', [0.6, 0.3, 0.1, 0.1]);
print('sensitivity-recs.eps', '-dpsc');
system('epstopdf sensitivity-recs.eps');
